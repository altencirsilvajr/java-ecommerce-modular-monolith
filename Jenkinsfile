pipeline {
  agent any
  tools { jdk 'temurin-25'; nodejs 'node-24' }
  stages {
    stage('Backend') { steps { sh './mvnw verify' } }
    stage('Frontend') { steps { sh 'npm --prefix frontend ci'; sh 'npm --prefix frontend run test:ci'; sh 'npm --prefix frontend run build' } }
    stage('Container') { steps { sh 'docker compose config --quiet'; sh 'docker build -t ecommerce-api:${BUILD_NUMBER} .' } }
  }
}
