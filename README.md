# webapp

## Technology Stack
1. Java 1.8
2. Spring Boot 2.2.4
3. PostgresSQL 
4. JUnit 4.12 

## Build Instructions
1. Build the project using Maven
2. Start the application from an IDE of choice or the command line
3. Spin up the PostgreSQL DB(sudo -u postgres psql)
4. Create a database with the name "users"
5. Create a user "postgres" and grant all permissions on users schema

## Deploy Instructions
Application is deployed locally and runs on http://localhost:8080/

## Running Tests
The unit tests can be found under the test package. The unit tests are developed using Junit 4.12

Install Postman - Demo of all the API implementation on Postman

## DNS Setup
### Create hosted zone for domain in ROOT AWS account
1. Register a domain name with a domain registrar such as Namecheap
2. Create a public hosted zone in Amazon Route 53.
3. Configure Namecheap to use custom nameservers provided by Amazon Route 53 to use the Route53 nameservers.
4. Create a type TXT record for your domain with TTL of 1 minute. Type TXT record should contain the text value "csye6225-spring2020"

### Create subdomain and hosted zone for DEV AWS account
1. Create a public hosted zone in Amazon Route 53 for the subdomain dev.yourdomainname.tld.
2. Configure nameservers for the subdomain in the root account. See docs.
3. Create a type TXT record for the subdomain with TTL of 1 minute. Type TXT record should contain the text value "csye6225-spring2020-dev".

### Create subdomain and hosted zone for PROD AWS account
1. Create a public hosted zone in Amazon Route 53 for the subdomain prod.yourdomainname.tld.
2. Configure nameservers for the subdomain in the root account. See docs.
3. Create a type TXT record for the subdomain with TTL of 1 minute. Type TXT record should contain the text value "csye6225-spring2020-prod".

### DNS updates to host application on cloud using load balancer
1. Route53 resource record for your domain name should now be an alias for your load balancer application.
2. Your CloudFormation template should configure Route53 so that your domain points to your load balancer and your web application is accessible thru http://your-domain-name.tld/.
3. Your application must be accessible using root context i.e. https://your-domain-name.tld/ and not https://your-domain-name.tld/app-0.1/

## JMeter Load Testing Script
1. Using Apache JMeter create tests that can be run against your application APIs.
2. JMeter tests need to make 500 concurrent API calls to your application to create bills.
3. All bills can be created under a single user account and the user account itself can be created via API call made outside of JMeter.

## User Stories
1. All API request/response payloads should be in JSON.
2. No UI should be implemented for the application.
3. As a user, I expect all APIs call to return with proper HTTP status code.
4. As a user, I expect the code quality of the application is maintained to highest standards using unit and/or integration tests.
5. Your web application must only support Token-Based authentication and not Session Authentication.
6. As a user, I must provide basic authentication token when making a API call to protected endpoint.

#### Create a new user
1. As a user, I want to create an account by providing information - Email Address, Password, First Name, Last Name and account_created field for the user should be set to current time when user creation is successful.
2. User should not be able to set values for account_created and account_updated. Any value provided for these fields must be ignored.
3. Password field should never be returned in the response payload.
4. As a user, I expect to use my email address as my username.
5. As a user, I expect application to enforce strong password as recommended by NIST.
6. Application must return 400 Bad Reqest HTTP response code when a user account with the email address already exists.
7. As a user, I expect my password to be stored securely using BCrypt password hashing scheme with salt.

#### Update user information
1. As a user, I want to update my account information. I should only be allowed to update fields - First Name, Last Name, Password
2. Attempt to update any other field should return 400 Bad Request HTTP response code.
3. account_updated field for the user should be updated when user update is successful.
4. A user can only update their own account information.

#### Get user information
1. As a user, I want to get my account information. 
2. Response payload should return all fields for the user except for password.

#### Bill information
1. As a user, I want to create new bills in the system.
2. As a user, I want to get my bills from the application. If I try to get a bills owned by someone else, application should return appropriate error.
3. As a user, I want to update a bills that I have created. If I try to update a bills owned by someone else, application should return appropriate error.
4. As a user, I want to delete a bills that I have created. If I try to delete a bills owned by someone else, application should return appropriate error.
5. As a user, I want to get, add, and delete attachments to the bill. I should be authenticated & authorized to be able to perform these operations.

#### Bill attachment as a file information
1. As a user, I want to add a file to my bill.
2. As a user, I want to delete file attached to my bill.
3. Application must support popular bill formats such as pdf, png, jpg, and jpeg.
4. As a user, I expect file to be stored in some directory on the web server.
5. Metadata (such as file name, size, upload date, file owner, bill it is attached to) about file attached to my bill should be stored in RDBMS such as MySQL.
6. Updating existing image requires deleting it first and then uploading a new image.

#### Cloud oriented web app updates:
1. As a user, I want to use the RDS instance on the cloud to store data when my application is running on EC2 instance.
2. As a user, I want to use S3 bucket to store user’s attachments instead of local disk.
3. As a user, I want the S3 object metadata to be stored in the database.
4. As a user, I want all application log data to be available in CloudWatch.
5. As a user, I want metrics on API usage available in CloudWatch.
6. Create following custom metrics for every API we have implemented in the web application. The metrics data should be collected in CloudWatch.
    a. Count number of times each API is called.
    b. Using Timer metrics data type, time (in milliseconds) each API call so we can understand how long it takes for the application to process an API call.
    c. Using Timer metrics data type, time (in milliseconds) each database query executed by your application.
    d. Using Timer metrics data type, time (in milliseconds) each call made to AWS S3 service by your application.
7. You can retrieve custom metrics using either StatsD or collectd.
8. CloudWatch agent configuration file must be copied over to the EC2 server when application is being deployed by CodeDeploy. You will also need to configure CloudWatch agent before starting your service.

##### API Endpoint To Get Bills Due
##### API Endpoint: Authenticated HTTP GET to /v1/bills/due/x

1. User should be able to request link to all of their recipe’s in the system via email.
2. As a user, I want to get list of bills due in next x days sent to me in an email.
3. HTTP request should return right away and not wait for processing to finish. Processing of the request should be handled in background. When a request is received, post message on SQS queue once the request has been authenticated and authorized.
4. I should only get one email within 60 minute window regardless of how many requests I make. 
5. Additional requests made by me in the 60 minute window should be ignored.
6. A separate thread in the application will poll the SQS queue and generate the list. It will then post the list of bills to a SNS topic which will trigger AWS Lambda function. Email will be sent to the user from the Lamdba function.