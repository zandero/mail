# mail
Utility to build a mail MimeMessage with Mustache templates support 

## Setup
```xml
<dependency>      
     <groupId>com.zandero</groupId>      
     <artifactId>mail</artifactId>      
     <version>1.0</version>      
</dependency>
```

## Usage

```java
MailMessage message = new MailMessage();
message.to("mail@email.com")
    .from("from@email.com")
    .subject("Test")
    .html("<p>Hello</p>");

MimeMessage mime = message.getMessage(session);
```

## Templates