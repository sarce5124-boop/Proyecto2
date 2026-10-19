CREATE DATABASE  IF NOT EXISTS `alquiler_vehiculos` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `alquiler_vehiculos`;
-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: alquiler_vehiculos
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `alquileres`
--

DROP TABLE IF EXISTS `alquileres`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `alquileres` (
  `id` int NOT NULL AUTO_INCREMENT,
  `cliente_id` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `vehiculo_placa` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha_inicio` date NOT NULL,
  `dias` int NOT NULL,
  `deposito_garantia` decimal(12,2) NOT NULL,
  `costo_alquiler` decimal(12,2) NOT NULL,
  `finalizado` tinyint(1) NOT NULL DEFAULT '0',
  `fecha_devolucion_real` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_alquiler_cliente` (`cliente_id`),
  KEY `fk_alquiler_vehiculo` (`vehiculo_placa`),
  CONSTRAINT `fk_alquiler_cliente` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`identificacion`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_alquiler_vehiculo` FOREIGN KEY (`vehiculo_placa`) REFERENCES `vehiculos` (`placa`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_costo` CHECK ((`costo_alquiler` >= 0)),
  CONSTRAINT `chk_deposito` CHECK ((`deposito_garantia` >= 0)),
  CONSTRAINT `chk_dias` CHECK ((`dias` > 0)),
  CONSTRAINT `chk_fecha_devolucion` CHECK (((`fecha_devolucion_real` is null) or (`fecha_devolucion_real` >= `fecha_inicio`))),
  CONSTRAINT `chk_finalizacion` CHECK ((((`finalizado` = false) and (`fecha_devolucion_real` is null)) or ((`finalizado` = true) and (`fecha_devolucion_real` is not null))))
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `alquileres`
--

LOCK TABLES `alquileres` WRITE;
/*!40000 ALTER TABLE `alquileres` DISABLE KEYS */;
INSERT INTO `alquileres` VALUES (1,'TEST002','TEST123','2026-10-19',3,5000.00,45000.00,1,'2026-10-21');
/*!40000 ALTER TABLE `alquileres` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clientes`
--

DROP TABLE IF EXISTS `clientes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `clientes` (
  `identificacion` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nombre` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `telefono` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `correo` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`identificacion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clientes`
--

LOCK TABLES `clientes` WRITE;
/*!40000 ALTER TABLE `clientes` DISABLE KEYS */;
INSERT INTO `clientes` VALUES ('TEST002','Cliente de Prueba 2','88888888','prueba2@gmail.com');
/*!40000 ALTER TABLE `clientes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vehiculos`
--

DROP TABLE IF EXISTS `vehiculos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehiculos` (
  `placa` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `marca` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tarifa_diaria` decimal(12,2) NOT NULL,
  `estado` enum('DISPONIBLE','ALQUILADO') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DISPONIBLE',
  `tipo` enum('MOTO','BICIMOTO','CUADRACICLO') COLLATE utf8mb4_unicode_ci NOT NULL,
  `cilindrada` int DEFAULT NULL,
  `tipo_motor` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tipo_traccion` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`placa`),
  CONSTRAINT `chk_atributos_por_tipo` CHECK ((((`tipo` = _utf8mb4'MOTO') and (`cilindrada` is not null) and (`tipo_motor` is null) and (`tipo_traccion` is null)) or ((`tipo` = _utf8mb4'BICIMOTO') and (`cilindrada` is null) and (`tipo_motor` is not null) and (`tipo_traccion` is null)) or ((`tipo` = _utf8mb4'CUADRACICLO') and (`cilindrada` is null) and (`tipo_motor` is null) and (`tipo_traccion` is not null)))),
  CONSTRAINT `chk_cilindrada_positiva` CHECK (((`cilindrada` is null) or (`cilindrada` > 0))),
  CONSTRAINT `chk_tarifa_positiva` CHECK ((`tarifa_diaria` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vehiculos`
--

LOCK TABLES `vehiculos` WRITE;
/*!40000 ALTER TABLE `vehiculos` DISABLE KEYS */;
INSERT INTO `vehiculos` VALUES ('TEST123','Yamaha',15000.00,'DISPONIBLE','MOTO',150,NULL,NULL);
/*!40000 ALTER TABLE `vehiculos` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-19 11:59:02
