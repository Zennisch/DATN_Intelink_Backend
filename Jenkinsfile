pipeline {
    agent {
        node {
            label 'intelink-agent'
        }
    }

    triggers {
        pollSCM('* * * * *')
    }

    stages {
        stage('Environment') {
            steps {
                echo 'Java version:'
                sh 'java --version'
                echo 'Maven version:'
                sh 'mvn --version'
                echo 'gcloud CLI version:'
                sh 'gcloud version'
            }
        }

        stage('Credentials') {
            steps {
                withCredentials([
                    file(credentialsId: 'INTELINK_ENV_FILE', variable: 'ENV_FILE'),
                    file(credentialsId: 'INTELINK_GCP_FILE', variable: 'GCP_FILE'),
                ]) {
                    echo 'Copying environment file to workspace...'
                    sh 'rm -f "$WORKSPACE/.env" "$WORKSPACE/gcp-key.json"'
                    sh 'cp "$ENV_FILE" $WORKSPACE/.env'
                    sh 'cp "$GCP_FILE" $WORKSPACE/gcp-key.json'
                    sh 'dos2unix $WORKSPACE/.env'
                }

                sh '. $WORKSPACE/.env && echo GCP_PROJECT_ID: $GCP_PROJECT_ID'
                echo 'Environment variables is set up successfully.'

                echo 'Setting up gcloud authentication...'
                sh '''
                    . $WORKSPACE/.env
                    gcloud auth activate-service-account --key-file="$WORKSPACE/gcp-key.json"
                    gcloud config set project $GCP_PROJECT_ID
                    gcloud projects describe $GCP_PROJECT_ID
                '''
                echo 'GCP authentication set up successfully.'
            }
        }

        stage('Build') {
            steps {
                echo 'Building the project with Maven...'
                sh 'cd $WORKSPACE/intelink-project && mvn clean package -DskipTests -B -q'
                echo 'Build completed successfully.'
            }
        }

        stage('Docker Build') {
            steps {
                echo 'Building Docker image...'
                sh '''
                    . $WORKSPACE/.env
                    docker build -t $GCP_IMAGE_URL $WORKSPACE/intelink-server --quiet
                '''
                echo 'Docker image built successfully.'
            }
        }

        stage('Push to Artifact Registry') {
            steps {
                echo 'Pushing Docker image to GCP Artifact Registry...'
                sh '''
                    . $WORKSPACE/.env
                    gcloud auth configure-docker $GCP_REGION-docker.pkg.dev --quiet
                    docker push $GCP_IMAGE_URL
                '''
                echo 'Docker image pushed successfully.'
            }
        }

        stage('Deploy to Cloud Run') {
            steps {
                echo 'Deploying to GCP Cloud Run...'
                sh '''
                    . $WORKSPACE/.env
                    gcloud auth activate-service-account --key-file="$WORKSPACE/gcp-key.json"
                    gcloud config set project $GCP_PROJECT_ID
                    gcloud run deploy $GCP_CLOUD_RUN_SERVICE_NAME \
                        --image $GCP_IMAGE_URL \
                        --platform managed \
                        --region $GCP_REGION \
                        --allow-unauthenticated \
                        --port 8080 \
                        --env-vars-file $WORKSPACE/.env \
                        --memory 2Gi \
                        --cpu 2 \
                        --min-instances 0 \
                        --max-instances 10 \
                        --timeout 600 \
                        --add-cloudsql-instances=$GCP_CLOUD_SQL_INSTANCE
                        --service-account $GCP_SERVICE_ACCOUNT_EMAIL
                '''
                echo 'Deployment completed successfully.'
            }
        }
    }
}
