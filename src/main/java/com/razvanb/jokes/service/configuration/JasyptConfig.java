package com.razvanb.jokes.service.configuration;//package com.razvanb.jokes.service.configuration;
//
//import org.jasypt.encryption.StringEncryptor;
//import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
//import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig;
//import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Profile;
//
//@Configuration
//@Profile("!test")
//public class JasyptConfig {
//
////    @Bean("jasyptStringEncryptor")
////    public StringEncryptor stringEncryptor() {
////        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
////        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
///
// /        config.setPassword(System.getenv("JASYPT_ENCRYPTOR_PASSWORD")); /
////        config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
////        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
////        config.setPoolSize(1);
////        encryptor.setConfig(config);
////        return encryptor;
////    }
//
//    @Bean
//    public StringEncryptor jasyptStringEncryptor() {
//        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
//        EnvironmentStringPBEConfig config = new EnvironmentStringPBEConfig();
//        config.setPassword(System.getenv("JASYPT_ENCRYPTOR_PASSWORD"));
//        config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
//        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
//        config.setPoolSize(1);
//        encryptor.setConfig(config);
//        return encryptor;
//    }
//}