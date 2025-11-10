# LeaseBridge

## Overview
This is a hobby project developed with the purpose of learning RAG infrastructure.

In this project I have learned following:
- How to upload a PDF document, extract into chunks and store it in vector database.
- How to fetch document from Docusign, extract into chunks and store in vector database.

Key capabilities:
- Read contracts from DocuSign using the DocuSign SDK. (To serve inital idea of capturing lease documents from Docusign and answer questions based off that)
- Enable users to upload their documents 
- Accept user file uploads and ingest them into the same storage/index.
- Provide a small HTTP API for listing documents, fetching raw PDFs, and triggering ingest.

## Modules
- `rag-module` — Overall rag implementation that provides following:
  - DocuSign integration: reads envelopes, lists documents, fetches documents, and saves them to local storage.
  - Ability to direcly upload a PDF document that is chunked, 

## Key components
- `DocusignController` (in `docusign-controller/ingest`): contains all API end-points that can be called to ingest and store a document

## CodeT93 comments:
Since Docusign was not a preferred mechanism to handle documents, following can be done:

- com.lb.docusign/ingest/DocusignController -> Based off requirements, Bulk-upload documents using the `/upload` 
  - or create a job that does it without `/upload` end-point or provide roles based ability to upload documents.
- com.lb.docusign/qa/QaController -> Make `/qa/answer` is used to answer all questions. Roles based access would still
  needed to be implemented 

## Build & run
In `rag_database` folder there is a dml script for all data tables used in this project.
- Build a database on a docker image
- Populate all properties in `application.yml` file. For localhost, application.yml can be used, however for production grade,
    each property needs to be loaded correctly (using chamber of secrets / secrets vault etc)

After all properties are loaded, from project root:
- Build everything:
  mvn clean install -DskipTests true; # I tested the application using personal documents that I needed to test with
  (both for unit tests), anyone using this project would need to setup their own unit tests.

Run assembled main app:
- Run the packaged jar:
  java -jar main-app/target/main-app-1.0.0-SNAPSHOT.jar

## Sample end-points
    1. # checks if application is running on localhost
        curl --location 'localhost:8080/docusign/healthz' 
    
    2. # provide a simple question and check answer
        curl --location --request GET 'http://localhost:8080/qa/answer?question=Can%20you%20summarize%20sample.pdf%20for%20me%3F' \
            --header 'Content-Type: application/x-www-form-urlencoded' \
            --data-urlencode 'question=what is this document about'
    
    3. #upload a document
        curl --location 'http://localhost:8080/docusign/upload' \
            --form 'file=@"\$HOME/Documents/test/sample.pdf"'
        

## Configuration
Example `application.yml` properties (in `src/main/resources` of modules):
```yaml
storage:
  root: /opt/leasebridge/data
docusign:
  accountId: YOUR_ACCOUNT_ID
  clientId: YOUR_CLIENT_ID
  clientSecret: YOUR_CLIENT_SECRET
server:
  port: 8080