-- MySQL dump 10.13  Distrib 8.0.15, for Win64 (x86_64)
--
-- Host: localhost    Database: db_zk
-- ------------------------------------------------------
-- Server version	8.0.15

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
 SET NAMES utf8 ;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `friend`
--

DROP TABLE IF EXISTS `friend`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `friend` (
  `ID` int(4) unsigned NOT NULL AUTO_INCREMENT COMMENT '好友关系表ID',
  `USER_ID` int(3) NOT NULL COMMENT '用户ID',
  `FRIEND_ID` int(100) NOT NULL COMMENT '好友ID',
  `USER_GROUP` varchar(15) NOT NULL COMMENT '用户分组',
  `FRIEND_GROUP` varchar(15) NOT NULL COMMENT '好友分组',
  PRIMARY KEY (`ID`),
  KEY `USERIDFINDROW` (`USER_ID`) USING BTREE COMMENT '通过USERID查找friend表信息'
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `friend`
--

LOCK TABLES `friend` WRITE;
/*!40000 ALTER TABLE `friend` DISABLE KEYS */;
INSERT INTO `friend` VALUES (1,1,2,'myfriend','stranger'),(2,2,3,'myfriends','myfriends'),(3,2,1,'myfriends','stranger'),(4,3,2,'myfriends','myfriends');
/*!40000 ALTER TABLE `friend` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `user` (
  `ID` int(100) unsigned NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `USERNAME` varchar(15) NOT NULL COMMENT '用户名',
  `PASSWORD` varchar(15) NOT NULL COMMENT '密码',
  `USERNICK` varchar(20) DEFAULT NULL COMMENT '用户昵称',
  `USERIMAGEPATH` varchar(50) DEFAULT NULL COMMENT '头像路径',
  `USERSTATE` char(1) DEFAULT '0' COMMENT '用户状态',
  `FIENDSUM` int(3) DEFAULT '0' COMMENT '好友数量',
  `GENDER` int(1) DEFAULT '0' COMMENT '用户性别',
  `PERSONLABEL` varchar(50) DEFAULT NULL COMMENT '个性签名',
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'user','pas','admin','C:\\Users\\zxk\\Pictures\\backGround\\icon\\icon2.png','0',0,0,'i am  master~'),(2,'zxk','zk001','AS','C:\\Users\\zxk\\Pictures\\backGround\\icon\\icon2.png','0',0,0,'hehehe~'),(3,'ctm','ctm001','CTT','C:\\Users\\zxk\\Pictures\\backGround\\icon\\icon1.png','0',0,0,'hahaha~');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-06-08 15:44:47
