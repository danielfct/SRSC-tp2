# Estrutura do projeto

srsc-tp2<br />
|-- pom.xml<br />
|-- vms.cer<br />
|-- gos.cer<br />
|-- clientconfig.conf<br />
|-- clientTrustStore<br />
|-- clientKeyStore.jceks<br />
|-- vmsconfig.conf<br />
|-- vmsKeyStore.jceks<br />
|-- gosconfig.conf<br />
|-- gosKeyStore.jceks<br />
`-- src<br />
    `-- client<br />
    |	|-- ClientConfiguration.java<br />
    |	|-- GOSAttestation.java<br />
    |	|-- VMSAttestation.java<br />
    |	|-- RedisData.java<br />
    |	|-- RedisClient.java<br />
    |	|-- JedisBenchmark.java<br />
    |	`-- exceptions<br />
    |       |-- AttestationException.java    <br />
    |       |-- BadReplyCodeException.java<br />
    |   	|-- UnexceptedServerNounceException.java<br />
    |       |-- UnverifiedServerSignatureException.java<br />
   	`-- tpm<br />
   	|	`-- vms<br />
    |   |	|-- VMSConfiguration.java<br />
    |   |	|-- VMSService.java<br />
    |   |	|-- VMSServer.java<br />
    |   |	|-- VMS_TPM.java<br />
    |	|	`-- exceptions<br />
    |   |    	|-- BadRequestCodeException.java    <br />
    |   |   	|-- DataReplyingException.java<br />
    |   `-- gos<br />
    |   |	|-- GOSConfiguration.java<br />
    |   |	|-- GOSService.java<br />
    |   |	|-- GOSServer.java<br />
    |   |	|-- GOS_TPM.java<br />
    |	|	`-- exceptions<br />
    |   |   	|-- BadRequestCodeException.java    <br />
    |   |    	|-- DataReplyingException.java
	|	|-- CommandUtils.java<br />
    `-- utils<br />
    	|-- GenerateSecretKey.java<br />
        |-- GenerateKeyPair.java<br />
    	|-- GenerateMacKey.java
        |-- Utils.java<br />


# ----- Ficheiros de Configuração -----

# pom.xml 

Define a propriedades e dependências do projeto maven<br />

# vms.cer

Certificado do serviço VMS-TPM, contém a chave pública do vms para verificar a assinatura digital do serviço<br />

# gos.cer

Certificado do serviço GOS-TPM, contém a chave pública do gos para verificar a assinatura digital do serviço<br />

# clientconfig.conf

Ficheiro de configuração do cliente, contém as propriedades necessárias para executar o cliente redis<br />

# clientTrustStore

Ficheiro que contém os certificados dos 2 serviços (VMS-TPM e GOS-TPM) necessários para efetuar conexão SSL/TLS<br />

# clientKeyStore.jceks

Contém a chave secreta, a chave privada (e pública) e chave mac do cliente usadas para garantir confidencialidade, autenticidade e integridade nas conexões com o redis<br />

# vmsconfig.conf

Contém todas as configurações do serviço VMS-TPM<br />

# vmsKeyStore.jceks

Ficheiro jceks que contém a chave privada (e pública) que o serviço vms utiliza para assinar a sua resposta ao cliente<br />

# gosconfig.conf

Contém todas as configurações do serviço GOS-TPM<br />

# gosKeyStore.jceks

Ficheiro jceks que contém a chave privada (e pública) que o serviço gos utiliza para assinar a sua resposta ao cliente<br />



# ----- Classes do Cliente -----

# ClientConfiguration.java

Carrega para a memória todas as configurações do cliente que estão no ficheiro clientconfig.conf<br />

# GOSAttestation.java

Estabelece uma conexão SSL/TLS com o serviço GOS-TPM para obter a sua atestação<br />

# VMSAttestation.java

Estabelece uma conexão SSL/TLS com o serviço VMS-TPM para obter a sua atestação<br />

# RedisData.java

Contém todos os dados necessários para a população inicial do redis<br />

# RedisClient.java

Executa operações GET/SET/DELETE ao redis utilizando o jedis<br />

# JedisBenchmark.java

Classe main do cliente, utilizando o RedisClient:<br />
	- executa a inserção de 100 entradas iniciais no redis,<br />
	- verifica as operações/segundo sem fazer atestação e<br />
	- verifica as operações/segundo fazendo atestação<br />

# exceptions
- AttestationException.java    <br />
- BadReplyCodeException.java<br />
- UnexceptedServerNounceException.java<br />
- UnverifiedServerSignatureException.java<br />




# ----- Classes do serviço VMS-TPM -----

# VMSConfiguration.java

Carrega para a memória todas as configurações do serviço vms que estão no ficheiro vmsconfig.conf<br />

# VMSService.java

Serviço vms que processa 1 pedido de atestação vms do cliente<br />

# VMSServer.java

Servidor vms recebe os pedidos do cliente e executa-os assincronamente utilizando uma pool de threads<br />

# VMS_TPM.java

Classe main do módulo VMS_TPM, inicia pelo menos um servidor vms<br />

# exceptions
- BadRequestCodeException.java    <br />
- DataReplyingException.java<br />



# ----- Classes do serviço GOS-TPM -----

# GOSConfiguration.java

Carrega para a memória todas as configurações do serviço gos que estão no ficheiro gosconfig.conf<br />

# GOSService.java

Serviço gos que processa 1 pedido de atestação gos do cliente<br />

# GOSServer.java

Servidor gos recebe os pedidos do cliente e executa-os assincronamente utilizando uma pool de threads<br />

# GOS_TPM.java

Classe main do módulo GOS-TPM, inicia pelo menos um servidor gos<br />

# exceptions
- BadRequestCodeException.java    <br />
- DataReplyingException.java<br />



# ----- Classes de utilidade -----

# CommandUtils.java

Classe de utilidade para executar comandos unix<br />

# GenerateSecretKey.java

Classe para gerar uma chave secreta que fica armazenada num ficheiro jceks (cria o ficheiro jceks se não existir)<br />
(alternativa ao keytools)<br />

# GenerateKeyPair.java

Classe para gerar um pair de chaves pública-privada que fica armazenada num ficheiro jceks (cria o ficheiro jceks se não existir)<br />
(alternativa ao keytools)<br />

# GenerateMacKey.java

Classe para gerar uma chave secreta mac que fica armazenada num ficheiro jceks (cria o ficheiro jceks se não existir)<br />
(alternativa ao keytools)<br />

# Utils.java

Classe que contém alguns métodos estáticos de utilidade<br />