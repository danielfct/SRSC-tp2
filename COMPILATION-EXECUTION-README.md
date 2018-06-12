# COMPILATION-EXECUTION-README

gerar par chave privada-publica para o cliente:
keytool -genkey -alias clientKeyPair -keyAlg RSA -keystore "C:\Users\ASUS\Desktop\FCT\Programacao\Java\workspace\SRSC-TP2\clientKeyStore.jceks" -keysize 2048 -storepass clientkeystorepassword

keytool -genseckey -alias secretKey -keyalg AES -keysize 256 -storetype jceks -keystore "C:\Users\ASUS\Desktop\FCT\Programacao\Java\workspace\SRSC-TP2\clientKeyStore.jceks"

keytool -genseckey -alias macKey -keyalg HMacSHA1 -keysize 256 -storetype jceks -keystore "C:\Users\ASUS\Desktop\FCT\Programacao\Java\workspace\SRSC-TP2\clientKeyStore.jceks"


gerar par chave privada-publica para o vms:
keytool -genkey -alias vmsKeyPair -keyAlg RSA -keystore "C:\Users\ASUS\Desktop\FCT\Programacao\Java\workspace\SRSC-TP2\vmsKeyStore.jceks" -keysize 2048 -storepass vmskeystorepassword

gerar certificado para o vms:
keytool -export -alias vmsKeyPair -keystore "C:\Users\ASUS\Desktop\FCT\Programacao\Java\workspace\SRSC-TP2\vmsKeyStore.jceks" -file "C:\Users\ASUS\Desktop\FCT\Programacao\Java\workspace\SRSC-TP2\vms.cer"

colocar o certificado do vms na truststore do client:
keytool -import -file C:\Users\ASUS\Desktop\FCT\Programacao\Java\workspace\SRSC-TP2\vms.cer -alias vms -keystore C:\Users\ASUS\Desktop\FCT\Programacao\Java\workspace\SRSC-TP2\clientTrustStore


gerar par chave privada-publica para o gos:
keytool -genkey -alias gosKeyPair -keyAlg RSA -keystore "C:\Users\ASUS\Desktop\FCT\Programacao\Java\workspace\SRSC-TP2\gosKeyStore.jceks" -keysize 2048 -storepass goskeystorepassword

gerar o certificado para o gos:
keytool -export -alias gosKeyPair -keystore "C:\Users\ASUS\Desktop\FCT\Programacao\Java\workspace\SRSC-TP2\gosKeyStore.jceks" -file "C:\Users\ASUS\Desktop\FCT\Programacao\Java\workspace\SRSC-TP2\gos.cer"

colocar o certificado do gos na truststore do client:
keytool -import -file C:\Users\ASUS\Desktop\FCT\Programacao\Java\workspace\SRSC-TP2\gos.cer -alias gos -keystore C:\Users\ASUS\Desktop\FCT\Programacao\Java\workspace\SRSC-TP2\clientTrustStore