# COMPILATION-EXECUTION-README


# Como eu gerei os ficheiros para o serviço VMS-TPM:

Gerar par chave privada-publica para o vms<br />
keytool -genkey -alias vmsKeyPair -keyAlg RSA -keystore "/home/osboxes/Desktop/SRSC/JedisBenchmark/vmsKeyStore.jceks" -keysize 2048 -storepass vmskeystorepassword

Gerar certificado para o vms<br />
keytool -export -alias vmsKeyPair -keystore "/home/osboxes/Desktop/SRSC/JedisBenchmark/vmsKeyStore.jceks" -file "/home/osboxes/Desktop/SRSC/JedisBenchmark/vms.cer"


# Como eu gerei os ficheiros para o serviço VMS-TPM:

Gerar par chave privada-publica para o gos<br />
keytool -genkey -alias gosKeyPair -keyAlg RSA -keystore "/home/osboxes/Desktop/SRSC/JedisBenchmark/gosKeyStore.jceks" -keysize 2048 -storepass goskeystorepassword

Gerar o certificado para o gos<br />
keytool -export -alias gosKeyPair -keystore "/home/osboxes/Desktop/SRSC/JedisBenchmark/gosKeyStore.jceks" -file "/home/osboxes/Desktop/SRSC/JedisBenchmark/gos.cer"


# Como eu gerei os ficheiros para o cliente:

Gerar par chave privada-publica para o cliente<br />
keytool -genkey -alias clientKeyPair -keyAlg RSA -keystore "/home/osboxes/Desktop/SRSC/JedisBenchmark/clientKeyStore.jceks" -keysize 2048 -storepass clientkeystorepassword

Gerar chave secreta para o cliente<br />
keytool -genseckey -alias secretKey -keyalg AES -keysize 256 -storetype jceks -keystore "/home/osboxes/Desktop/SRSC/JedisBenchmark/clientKeyStore.jceks"

Gerar chave mac para o cliente<br />
keytool -genseckey -alias macKey -keyalg HMacSHA1 -keysize 256 -storetype jceks -keystore "/home/osboxes/Desktop/SRSC/JedisBenchmark/clientKeyStore.jceks"

Colocar o certificado do serviço VMS-TPM na truststore do client<br />
keytool -import -file /home/osboxes/Desktop/SRSC/JedisBenchmark/vms.cer -alias vms -keystore /home/osboxes/Desktop/SRSC/JedisBenchmark/clientTrustStore

colocar o certificado do serviço GOS-TPM na truststore do client<br />
keytool -import -file /home/osboxes/Desktop/SRSC/JedisBenchmark/gos.cer -alias gos -keystore /home/osboxes/Desktop/SRSC/JedisBenchmark/clientTrustStore


# Criar o projeto maven

Na diretoria principal do projeto<br />
mvn package

# Executar o redis-server

Na pasta src do redis<br />
./redis-server

# Executar o servidor VMS-TPM

Na diretoria principal do projeto<br />
java -cp target/JedisBenchmark-1.0-SNAPSHOT.jar tpm.vms.VMS_TPM 4444 /home/osboxes/Desktop/SRSC/JedisBenchmark/vmsconfig.conf

# Executar o servidor GOS-TPM

Na diretoria principal do projeto<br />
java -cp target/JedisBenchmark-1.0-SNAPSHOT.jar tpm.gos.GOS_TPM 4445 /home/osboxes/Desktop/SRSC/JedisBenchmark/gosconfig.conf

# Executar o cliente

Na diretoria principal do projeto<br />
java -Djavax.net.ssl.trustStore=/home/osboxes/Desktop/SRSC/JedisBenchmark/clientTrustStore -cp target/JedisBenchmark-1.0-SNAPSHOT.jar client.JedisBenchmark /home/osboxes/Desktop/SRSC/JedisBenchmark/clientconfig.conf 100

