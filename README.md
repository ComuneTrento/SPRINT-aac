# AAC
Authentication and Authorization Control Module.

This module exposes the OAuth2.0 protocol functionality for user authentication and
for the authorization of resource access (e.g., APIs). The module allows for managing 
user registration and for athenticating user using social accounts, in particular Google+ and Facebook. 

In SIMPATICO project is used as a Identity Server and allows for federating different components of 
the platform (e.g., IFE, Citizenpedia, etc). It is also used to protect the access to the platform
component APIs (e.g., logging, CDV, SF).

## Installation Requirements
- RDBMS (tested on MySQL 5.5+)
- Java 1.7+
- J2EE Servlet Container (tested on Apache Tomcat 7.*+)
- Apache Maven (3.0+)

## Installation

**MySQL Installation**
  1. Create a new schema (for example **acprovider**).
  2. Create a new user, (for example name **ac** password **ac**).
  3. Give the user all the privileges for the new schema.

The default properties may be changed in */main/resources/commoncore.properties* 

**Build Project**

The project is implemented to be built with Apache Maven. To build the project, execute the following command:

<code>
./mvnw package
</code> 

This will create the *aac.war* in *target* folder. The arcihve is ready for deployment in standard servlet container. 
The AAC management console is available at 

   http://localhost:8080/aac
 
## Generic Configuration

The configuration properties of the project are available in */main/resources/commoncore.properties*. They include

**DB configuration** 

Defined with *jdbc.+* properties. 
 
**External authentication** 

To configure the authentication using social accounts (i.e., Google or Facebool) it is necessary to specify the 
corresponding properties namely *google.+* and *fb.+*. Refer to the corresponding provider documentation for the instructions on how to obtain the clientId and client secrets for Google and Facebook. 

**External URL** 

If the app is exposed externally on a custom domain, the *application.url* property should declare the AAC endpoint on that domain, e.g. *https://example.com/aac*.
 
**Access to the developer console** 

The development console (/aac/dev) is used to configure AAC OAuth2 client applications that use AAC as Authorization Server (e.g., IFE, CDV, Citizenpedia). By default any user may access and create the client applications. In order to avoid this, set *mode.restricted* property to true and configure the enabled accounts in the authorized account list (defined with the *ac.admin.file* property). Specified the authorized accounts in this file as follows (one account role per line):

<code>
 < authority>;< attrkey>;< attrvalue>;< role>
</code> 

where **authority** stands for *google* or *facebook*, **attrkey** stands for identity attribute for that authority (*OIDC_CLAIM_email* and *id* for Google and Facebook respectively), **attrvalue** defines the value of the attribute, and **role** defines the possible role (one of *admin* or *developer*). For example,

<code>
  google;OIDC_CLAIM_email;abc@gmail.com;admin
</code> 

associates the role *admin* to the user with email abc@gmail.com if authenticated with Google account.

**Support Email**

Support email address to be contacted in case of error or administration issues. It will be displayed on error pages.
   
## Usage Scenarios

### 1. Using AAC for Authentication

This is a scenario where AAC is used as an Idenity Provider in a federated authentication environment. This is the case, for example, for IFE authentication or Citizenpedia.

To enable this scenario, AAC exposes OAuth2 protocol. Specifically, it is possible to use *OAuth2.0 Implicit Flow* as follows.


**1.1. Register Client App in the AAC developer console (*/aac/dev*).** 

To do this
- Login with authorized account (see access configuration above);
- Click *New App* and specify the corresponding client name
- In the *Settings* tab check *Server-side* and *Browser access* and select the identity providers to be used 
 for user authentication (e.g., google). Specify also a list of allowed redirect addresses (comma separated).
- In the *Permissions* tab select *Basic profile service* and check *profile.basicprofile.me* and 
 *profile.accountprofile.me* scopes. These scopes are necessary to obtain the information of the currently signed 
 user using AAC API.
         
**1.2. Activate Implicit Flow Authorization**

This flow is suitable for the scenarios, where the client application (e.g., client part of a Web appp) makes the authentication and then direct access to the API without passing through its own Web server backend. This allows for generating only a token for a short time period, so the next time the API access is required, the authentication should be performed again. In a nutshell, the flow is realized as follows:    

- The client app, when there is a need for the token, emits an authorization request to AAC in a browser window.
  <code>https://dev.welive.eu/aac/eauth/authorize</code>.
  
  The request accepts the following set of parameters:
     - *client_id*: the client_id obtained in developer console Indicates the client that is making the request. 
       The value passed in this parameter must exactly match the value in the console.
     - *response_type* with value *token*,  which determines if the OAuth 2.0 endpoint returns a token.
     - *redirect_uri*: URL to which the AAC will redirect upon user authentication and authorization. 
       The value of this parameter must exactly match one of the values registered in the APIs Console 
       (including the http or https schemes, case , and trailing ‘/’).
     - *scope*: space-delimited set of permissions the application requests Indicates the access your 
        application is requesting. 

- AAC redirects the user to the authentication page, where the user selects one of the identity providers and performs
  the sign in.
- Once authenticated, AAC asks the user whether the permissions for the requested operations may be granted.
- If the user accepts, the browser is redirected to the specified redirect URL, attaching the token data in the 
url hash part:  
  <code>http://www.example.com#access\_token=025a90d4-d4dd-4d90-8354-779415c0c6d8&token\_type=Bearer&expires\_in=3600</code>.

- Use the obtained token to obtain user data using the AAC API:


      GET /aac/basicprofile/me HTTPS/1.1 
      Host: aacserver.com 
      Accept: application/json 
      Authorization: Bearer 025a90d4-d4dd-4d90-8354-779415c0c6d8


  The result of the invocation describes basic user properties (e.g., userId) that can be used to uniquely identify the 
  user.
  
  
### 2. Using AAC for Securing Resoures and Services


In this scenario the goal is to restrict access to the protected resources (e.g., an API endpoint). Also in this case
the scenario relies on the use of OAuth2 protocol. The two cases are considered here:

- The protected resources deal with user-related data or operation. For example, in case the access to the user
profile is performed, access to CDV is performed on behalf of a specific user, etc. In this case (according to OAuth2), the access to the API should be accompanied with the *Authorization* header that contains the access token obtained via Implicit Flow (or via Authorization Code Flow).

- The protected resource does not deal with user-related data or operation and is not performed client-side. In this case, the access to the API should 
  also be accompanied with the *Authorization* header that contains the access token obtained via OAuth2 client  
  credentials flow.
  
The protected resource will use the OAuth2 token and dedicated AAC endpoints to ensure that the token is valid and (in case of user-related data) to verify user identity.  If the token is not provided or it is not valid, the protected resource should return 401 (Unauthorized) error to the caller. 

**2.1. AAC API**
  
To obtain the user data the following call should be performed:   

    GET /aac/basicprofile/me HTTPS/1.1 
    Host: aacserver.com 
    Accept: application/json 
    Authorization: Bearer <token-value>  
  
If the token is valid, this returns the user data:

    {
    "name": "Mario",
    "surname": "Rossi",
    "userId": "6789"
    }  

To validate the token, i.e., to check the token is not expired and is associated to proper scopes the following call
should be performed (optionally, the scope to be checked is passed as *scope* query parameter, comma-separated):

    GET /aac/resources/access HTTPS/1.1 
    Host: aacserver.com 
    Accept: application/json 
    Authorization: Bearer <token-value>  
    scope=profile.basicprofile.me

The request return value of true (respectively, false) if the token is valid and is applicable for the specified scope.


**2.2. Generating Client Credentials Flow Token**

In case the access to the non user-resource is performed, it is possible to use access token obtained through
OAuth2 client credentials flow. In this flow, the resulting token is associated to an client application only. 

The simplest way to obtain such token is through the AAC development console: on the *Overview* page of the client app use the *Get client credentials flow token* link to generate the access token. Note that the token is not expiring and therefore may be reused.

Alternatively, the token may be obtained through the AAC Oauth2 token endpoint call:

    POST /aac/oauth/token HTTPS/1.1
    Host: aacserver.com 
    Accept: application/json 
    client_id=23123121sdsdfasdf3242&
    client_secret=3rwrwsdgs4sergfdsgfsaf&
    grant_type=client_credentials
    
The following parameters should be passed:

- *grant_type*: value *client_credentials*
- *client_id*: client app ID
- *client_secret*: client secret

A successful response is returned as a JSON object, similar to the following:

    "access_token": "025a90d4-d4dd-4d90-8354-779415c0c6d8",
    "token_type": "bearer",
    "expires_in": 38937,
    "scope": "profile.basicprofile.all"      
    
    
**2.3. Internationalization**
In order to adapt AAC in different language, it is required to configure the parameter (default_locale)
inside commoncore.properties file

	# default locale.
	defaultLocale=it

The resource strings for selected locale can be defined inside resources folder

	internal_it.properties
	
(Optional) to be used with Internal Login feature. One can also create internal login account registration, password recovery  templates in different languages following the existing ones inside folder:

	src/main/resources/templates
	

**2.4. Internal Login (Optional)**

Additional internal login can be activated by setting the parameter(internal.account.enabled) inside
commoncore.properties file:

	# internal account.
	internal.account.enabled=true

This allow enabling of internal account management facility. The functionality requires configruation of
mail sender configuration inside the commoncore.properties file. With this configuration parameters, one can specify the principal account to be used for sending email on account registration and password recovery:

	# smtp configuration(with internal account enabled).
	mail.username=<email>
	mail.password=<pass>
	mail.host=smtp.gmail.com
	mail.port=587
	mail.protocol=smtp
	


specify the email account