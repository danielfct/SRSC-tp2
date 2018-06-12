# Estrutura do projeto

srsc-tp2  
|-- pom.xml  
|-- vms.cer  
|-- gos.cer 
|-- clientconfig.conf  
|-- clientTrustStore  
|-- clientKeyStore.jceks  
|-- vmsconfig.conf  
|-- vmsKeyStore.jceks  
|-- gosconfig.conf  
|-- gosKeyStore.jceks  
|-- src  
    |-- client  
    |	|-- ClientConfiguration.java  
    |	|-- GOSAttestation.java  
    |	|-- VMSAttestation.java  
    |	|-- RedisData.java  
    |	|-- RedisClient.java  
    |	|-- JedisBenchmark.java  
    |	|-- exceptions  
    |       |-- AttestationException.java      
    |       |-- BadReplyCodeException.java  
    |   	|-- UnexceptedServerNounceException.java  
    |       |-- UnverifiedServerSignatureException.java  
   	|-- tpm  
   	|	|-- vms  
    |   |	|-- VMSConfiguration.java  
    |   |	|-- VMSService.java  
    |   |	|-- VMSServer.java  
    |   |	|-- VMS_TPM.java  
    |	|	|-- exceptions  
    |   |    	|-- BadRequestCodeException.java      
    |   |   	|-- DataReplyingException.java  
    |   |-- gos  
    |   |	|-- GOSConfiguration.java  
    |   |	|-- GOSService.java  
    |   |	|-- GOSServer.java  
    |   |	|-- GOS_TPM.java  
    |	|	|-- exceptions  
    |   |   	|-- BadRequestCodeException.java      
    |   |    	|-- DataReplyingException.java
	|	|-- CommandUtils.java  
    |-- utils  
    	|-- GenerateSecretKey.java  
        |-- GenerateKeyPair.java  
    	|-- GenerateMacKey.java
        |-- Utils.java  


# ----- Ficheiros de Configuração -----

# pom.xml 

Define a propriedades e dependências do projeto maven  

# vms.cer

Certificado do serviço VMS-TPM, contém a chave pública do vms para verificar a assinatura digital do serviço  

# gos.cer

Certificado do serviço GOS-TPM, contém a chave pública do gos para verificar a assinatura digital do serviço  

# clientconfig.conf

Ficheiro de configuração do cliente, contém as propriedades necessárias para executar o cliente redis  

# clientTrustStore

Ficheiro que contém os certificados dos 2 serviços (VMS-TPM e GOS-TPM) necessários para efetuar conexão SSL/TLS  

# clientKeyStore.jceks

Contém a chave secreta, a chave privada (e pública) e chave mac do cliente usadas para garantir confidencialidade, autenticidade e integridade nas conexões com o redis  

# vmsconfig.conf

Contém todas as configurações do serviço VMS-TPM  

# vmsKeyStore.jceks

Ficheiro jceks que contém a chave privada (e pública) que o serviço vms utiliza para assinar a sua resposta ao cliente  

# gosconfig.conf

Contém todas as configurações do serviço GOS-TPM  

# gosKeyStore.jceks

Ficheiro jceks que contém a chave privada (e pública) que o serviço gos utiliza para assinar a sua resposta ao cliente  



# ----- Classes do Cliente -----

# ClientConfiguration.java

Carrega para a memória todas as configurações do cliente que estão no ficheiro clientconfig.conf  

# GOSAttestation.java

Estabelece uma conexão SSL/TLS com o serviço GOS-TPM para obter a sua atestação  

# VMSAttestation.java

Estabelece uma conexão SSL/TLS com o serviço VMS-TPM para obter a sua atestação  

# RedisData.java

Contém todos os dados necessários para a população inicial do redis  

# RedisClient.java

Executa operações GET/SET/DELETE ao redis utilizando o jedis  

# JedisBenchmark.java

Classe main do cliente, utilizando o RedisClient:  
	- executa a inserção de 100 entradas iniciais no redis,  
	- verifica as operações/segundo sem fazer atestação e  
	- verifica as operações/segundo fazendo atestação  

# exceptions
- AttestationException.java      
- BadReplyCodeException.java  
- UnexceptedServerNounceException.java  
- UnverifiedServerSignatureException.java  




# ----- Classes do serviço VMS-TPM -----

# VMSConfiguration.java

Carrega para a memória todas as configurações do serviço vms que estão no ficheiro vmsconfig.conf  

# VMSService.java

Serviço vms que processa 1 pedido de atestação vms do cliente  

# VMSServer.java

Servidor vms recebe os pedidos do cliente e executa-os assincronamente utilizando uma pool de threads  

# VMS_TPM.java

Classe main do módulo VMS_TPM, inicia pelo menos um servidor vms  

# exceptions
- BadRequestCodeException.java      
- DataReplyingException.java  



# ----- Classes do serviço GOS-TPM -----

# GOSConfiguration.java

Carrega para a memória todas as configurações do serviço gos que estão no ficheiro gosconfig.conf  

# GOSService.java

Serviço gos que processa 1 pedido de atestação gos do cliente  

# GOSServer.java

Servidor gos recebe os pedidos do cliente e executa-os assincronamente utilizando uma pool de threads  

# GOS_TPM.java

Classe main do módulo GOS-TPM, inicia pelo menos um servidor gos  

# exceptions
- BadRequestCodeException.java      
- DataReplyingException.java  



# ----- Classes de utilidade -----

# CommandUtils.java

Classe de utilidade para executar comandos unix  

# GenerateSecretKey.java

Classe para gerar uma chave secreta que fica armazenada num ficheiro jceks (cria o ficheiro jceks se não existir)  
(alternativa ao keytools)  

# GenerateKeyPair.java

Classe para gerar um pair de chaves pública-privada que fica armazenada num ficheiro jceks (cria o ficheiro jceks se não existir)  
(alternativa ao keytools)  

# GenerateMacKey.java

Classe para gerar uma chave secreta mac que fica armazenada num ficheiro jceks (cria o ficheiro jceks se não existir)  
(alternativa ao keytools)  

# Utils.java

Classe que contém alguns métodos estáticos de utilidade  