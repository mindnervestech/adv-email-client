-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               5.6.14 - MySQL Community Server (GPL)
-- Server OS:                    Win32
-- HeidiSQL Version:             8.0.0.4396
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- Dumping structure for table mail.basic_info
CREATE TABLE IF NOT EXISTS `basic_info` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `channel_name` varchar(100) DEFAULT NULL,
  `publisher` varchar(100) DEFAULT NULL,
  `publisher_url` varchar(100) DEFAULT NULL,
  `media_kit_url` varchar(100) DEFAULT NULL,
  `username` varchar(100) DEFAULT NULL,
  `password` varchar(100) DEFAULT NULL,
  `last_renewed` datetime DEFAULT NULL,
  `history` varchar(100) DEFAULT NULL,
  `notes` longtext,
  `subscriber` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- Dumping data for table mail.basic_info: ~3 rows (approximately)
/*!40000 ALTER TABLE `basic_info` DISABLE KEYS */;
INSERT INTO `basic_info` (`id`, `channel_name`, `publisher`, `publisher_url`, `media_kit_url`, `username`, `password`, `last_renewed`, `history`, `notes`, `subscriber`) VALUES
	(1, 'email.globalspec.com', 'dalave', 'oonjnan-', 'ojkij', 'ob', 'bnn', '2014-10-14 00:00:00', 'b', 'bjklk', 'b'),
	(2, 'quora.com', 'llk', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
	(3, 'advanstarchromatograph.com', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
/*!40000 ALTER TABLE `basic_info` ENABLE KEYS */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
