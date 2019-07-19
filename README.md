# DynatraceWorkshop2

## 1 - Description du projet
L'objectif de ce document est de:
1) déployer une application web multi-conteneurs dans Kubernetes sur Azure et instrumenter ce cluster Kubernetes avec le OneAgentOperator
2) instrumenter avec Dynatrace une application mobile Android communiquant avec cette application

### 1.a - Présentation de l'application

L'application est une simple application test Microsoft où l'on peut voter pour son animal préféré. <br>

![Alt text](images/AzureVotingApp_webInterface.PNG?raw=true "Title")

### 1.b - Technologies utilisées

#### Docker

#### Azure

#### Kubernetes

## 2 - Pré-requis: Souscrire à un compte Azure 

Demander à GTG l’activation de visual-studio-msdn-activation sur votre compte Dynatrace. Une fois activé, vous bénéficierez de 45€ de crédit Azure par mois, bloqués et sans besoin de mettre de carte de crédit: https://gtg.dynatrace.com/solutions/609798-visual-studio-msdn-activation

## 3 - Préparer une application pour Azure Kubernetes Service (AKS)
(Windows)

### Etape 1: Télécharger le code source de l'application

Aller à l'adresse https://github.com/Azure-Samples/azure-voting-app-redis.git et cliquer sur le bouton "Clone or Download" puis "Download ZIP" pour télécharger le projet.

### Etape 2: Tester l’application multiconteneurs dans un environnement Docker local

Dans le répertoire azure-voting-app-redis se trouvent le code source de l’application, un fichier Docker Compose précréé et un fichier manifeste Kubernetes. Vous pouvez utiliser Docker Compose pour automatiser la création d’images conteneur et le déploiement d’applications multiconteneurs.

#### 2.1 - Créer l'image conteneur et démarrer l'application
Après avoir installer Docker Compose: https://docs.docker.com/compose/install/, la commande suivante vous permet de créer l’image conteneur, téléchargez l’image Redis, puis démarrez l’application localement.
```shell
$ docker-compose up -d
```
Le fichier docker-compose.yaml définit les services de votre application afin de pouvoir les exécuter ensemble dans une environnement isolé.

#### 2.2 - Vérifier que les conteneurs ont bien été crées
Une fois terminé, utilisez la commande *docker images* pour afficher les images créée (3 images ont été téléchargées ou créées) :
```shell
$ docker images

REPOSITORY                   TAG        IMAGE ID            CREATED             SIZE
azure-vote-front             latest     9cc914e25834        40 seconds ago      694MB
redis                        latest     a1b99da73d05        7 days ago          106MB
tiangolo/uwsgi-nginx-flask   flask      788ca94b2313        9 months ago        694MB
```
Exécutez la commande *docker ps* pour voir les conteneurs en cours d’exécution :
```shell
$ docker ps

CONTAINER ID        IMAGE             COMMAND                  CREATED             STATUS              PORTS                           NAMES
82411933e8f9        azure-vote-front  "/usr/bin/supervisord"   57 seconds ago      Up 30 seconds       443/tcp, 0.0.0.0:8080->80/tcp   azure-vote-front
b68fed4b66b6        redis             "docker-entrypoint..."   57 seconds ago      Up 30 seconds
```

#### 2.3 - Ouvrir l'application localement
Pour voir l’application en cours d’exécution, entrez http://localhost:8080 dans un navigateur web local.

#### 2.4 - Supprimer des ressources
Maintenant que la fonctionnalité de l’application a été validée, les conteneurs en cours d’exécution peuvent être arrêtés et supprimés. <br/>  
Arrêtez et supprimez les instances et ressources de conteneur avec la commande *docker-compose down* :
```shell
docker-compose down
```
:exclamation: Ne supprimez pas les images de conteneur. Lorsque l’application locale a été supprimée, vous disposez d’une image Docker qui contient l’application Azure Vote, azure-vote-front.

## 4 - Déployer et utiliser Azure Container Registry

### Etape 0
1) Connectez vous avec votre compte Dynatrace au portal Azure: https://portal.azure.com et vérifiez que vous êtes bien souscrit. <br/>
![Alt text](images/azure_subscription.PNG?raw=true "Title")

2) Téléchargez l'Azure CLI sur votre machine: https://aka.ms/installazurecliwindows
3) Ouvrez un terminal Windows et connectez vous à votre compte Dynatrace avec la commande *az login*. Si la page d'authentification ne s'affiche pas automatiquement, allez sur https://aka.ms/devicelogin:
```shell
az login
```

### Etape 1: Création d’un Azure Container Registry

1) Créez un groupe de ressources nommé *myResourceGroup* créé dans la région *eastus*:
```shell
az group create --name myResourceGroup --location eastus
```
2) Créez une instance Azure Container Registry nommé *myContainerRegistryName*:
```shell
az acr create --resource-group myResourceGroup --name myContainerRegistryName --sku Basic
```
3) Connectez vous à ce registre (la commande devrait retourner le message *Login Succeeded*):
```shell
az acr login --name myContainerRegistryName
```

### Etape 2: Baliser une image conteneur et l'envoyer à une instance ACR
  Afficher la liste des images locales actuelles: 
  ```shell
  $ docker images

  REPOSITORY                   TAG                 IMAGE ID            CREATED             SIZE
  azure-vote-front             latest              4675398c9172        13 minutes ago      694MB
  redis                        latest              a1b99da73d05        7 days ago          106MB
  tiangolo/uwsgi-nginx-flask   flask               788ca94b2313        9 months ago        694MB
  ```
  Obtenez l’adresse du serveur de connexion (acrLoginServer):
  ```shell
  $ az acr list --resource-group myResourceGroup --query "[].{acrLoginServer:loginServer}" --output table
  ```
  Balisez votre image azure-vote-front locale avec l'acrLoginServer:
  ```shell
  $ docker tag azure-vote-front <acrLoginServer>/azure-vote-front:v1
  ```
  Vérifier que les étiquettes sont appliquées: 
  ```shell
  $ docker images
  
  REPOSITORY                                           TAG           IMAGE ID            CREATED             SIZE
  azure-vote-front                                     latest        eaf2b9c57e5e        8 minutes ago       716 MB
  mycontainerregistry.azurecr.io/azure-vote-front      v1            eaf2b9c57e5e        8 minutes ago       716 MB
  redis                                                latest        a1b99da73d05        7 days ago          106MB
  tiangolo/uwsgi-nginx-flask                           flask         788ca94b2313        8 months ago        694 MB
  ```

  Envoyez l'image à votre instance ACR:
  ```shell
  docker push <acrLoginServer>/azure-vote-front:v1
  ```

### Etape 3: Vérifier que l'image a bien été ajouté au registre
Liste des images qui ont été envoyées à votre instance ACR:
```shell
az acr repository list --name myContainerRegistryName --output table
```
```shell
Result
----------------
azure-vote-front
```

Les étiquettes d’une image spécifique:
```shell
az acr repository show-tags --name myContainerRegistryName --repository azure-vote-front --output table
```
```shell
Result
--------
v1
```

## 5 - Déployer un cluster Azure Kubernetes Service (AKS)

### Etape 1: Créer un principal du service
```shell
az ad sp create-for-rbac --skip-assignment
```
```shell
{
  "appId": "e7596ae3-6864-4cb8-94fc-20164b1588a9",
  "displayName": "azure-cli-2018-06-29-19-14-37",
  "name": "http://azure-cli-2018-06-29-19-14-37",
  "password": "52c95f25-bd1e-4314-bd31-d8112b293521",
  "tenant": "72f988bf-86f1-41af-91ab-2d7cd011db48"
}
```
Prenez note des valeurs de appId et de password.

### Etape 2: Configurer une authentification ACR

Obtenez l’ID de ressource ACR et mettez à jour le nom de Registre <acrName> avec celui de votre instance ACR et le groupe de ressources où se trouve cette instance:
```shell
az acr show --resource-group myResourceGroup --name myContainerRegistryName --query "id" --output tsv
```
Pour accorder l’accès qui permettra au cluster AKS de tirer (pull) des images stockées dans ACR, attribuez le rôle AcrPull: 
```shell
az role assignment create --assignee <appId> --scope <acrId> --role acrpull
```
  
  
### Etape 3: Créer un cluster Kubernetes
Créez un cluster AKS: 
```shell
az aks create --resource-group myResourceGroup --name myAKSCluster --node-count 1 --service-principal <appId> --client-secret <password> --generate-ssh-keys
```

### Etape 4: Installer l’interface de ligne de commande Kubernetes
```shell
az aks install-cli
```

### Etape 5: Se connecter au cluster à l’aide de kubectl
```shell
az aks get-credentials --resource-group myResourceGroup --name myAKSCluster
```
Vérifier la connexion à votre cluster:
```shell
$ kubectl get nodes

NAME                       STATUS   ROLES   AGE     VERSION
aks-nodepool1-28993262-0   Ready    agent   3m18s   v1.9.11
```
## 6 - Exécuter des applications dans Azure Kubernetes Service (AKS)

