// Copyright 2018 The Alephium Authors
// This file is part of the alephium project.
//
// The library is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// The library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with the library. If not, see <http://www.gnu.org/licenses/>.

package org.alephium.flow.setting

import java.io.File
import java.nio.file.{Files => JFiles, Path}

import scala.util.control.Exception.allCatch

import com.typesafe.config.{Config, ConfigException, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging

import org.alephium.protocol.config.{ConsensusConfigs, GroupConfig, NetworkConfig}
import org.alephium.protocol.model.{Block, ChainIndex, NetworkId, Transaction}
import org.alephium.protocol.vm.LockupScript
import org.alephium.serde.deserialize
import org.alephium.util._
import java.nio.charset.StandardCharsets

import java.io.{BufferedInputStream, FileNotFoundException}
import java.nio.file.{Files => NioFiles, Path, StandardOpenOption}

object ResourceIO extends StrictLogging {
  private def openResource(resourcePath: String) = {
    // Try both "/path" and "path"
    val p1 = resourcePath
    val p2 = resourcePath.stripPrefix("/")

    Option(getClass.getResourceAsStream(p1))
      .orElse(Option(getClass.getClassLoader.getResourceAsStream(p1)))
      .orElse(Option(getClass.getResourceAsStream("/" + p2)))
      .orElse(Option(getClass.getClassLoader.getResourceAsStream(p2)))
      .map(new BufferedInputStream(_))
      .getOrElse(throw new FileNotFoundException(s"Resource not found on classpath: $resourcePath"))
  }

  def copyFromResource(resourcePath: String, dest: Path): Unit = {
    logger.info(s"Copying resource $resourcePath to $dest")
    val in  = openResource(resourcePath)
    logger.info(s"in: $in")
    try {
      val parent = dest.getParent
      logger.info(s"parent: $parent")
      if (parent != null) NioFiles.createDirectories(parent)
      val out = NioFiles.newOutputStream(dest,
        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)
      try discard(in.transferTo(out)) finally out.close()
    } finally in.close()
  }
}

@SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
object Configs extends StrictLogging {
  private def check(port: Int): Boolean = {
    port > 0x0400 && port <= 0xffff
  }

  def validatePort(port: Int): Either[String, Unit] = {
    if (check(port)) Right(()) else Left(s"Invalid port: $port")
  }

  def validatePort(portOpt: Option[Int]): Either[String, Unit] = {
    portOpt match {
      case Some(port) => validatePort(port)
      case None       => Right(())
    }
  }

  def getConfigTemplate(
      rootPath: Path,
      confName: String,
      templateName: String,
      overwrite: Boolean
  ): File = {
    logger.info(s"Loading configuration template")
    logger.info(s"Loading $confName configuration template: $templateName")
    logger.info(s"Root path: $rootPath")
    val file = getConfigFile(rootPath, confName)

    logger.info(s"Configuration file path: $file")

    logger.info(s"Overwrite: $overwrite")
    logger.info(s"File exists: ${file.exists()}")
    if (overwrite && file.exists()) {
      logger.info(s"Deleting $file")
      file.delete()
    }

    if (!file.exists()) {
      logger.info(s"Creating new ${file.toPath} from template $templateName")
//       val templateContent = this.getClass.getResourceAsStream(s"/$templateName.conf.tmpl")
//       logger.info(s"Template content: $templateContent")
//       val templateContent2 = this.getClass.getResourceAsStream(s"$templateName.conf.tmpl")
//       logger.info(s"Template content2: $templateContent2")
//       val templateContent3 = this.getClass.getResourceAsStream(s"/flow/src/main/resources/$templateName.conf.tmpl")
//       logger.info(s"Template content3: $templateContent3")
//       val templateContent4 = this.getClass.getResourceAsStream(s"flow/src/main/resources/$templateName.conf.tmpl")
//       logger.info(s"Template content4: $templateContent4")
// val in = getClass.getResourceAsStream("/system_prod.conf.tmpl")
// logger.error(if (in != null) "Found!" else "Missing!")
//       logger.info(s"in: $in")
//       val inContent = scala.io.Source.fromInputStream(in, "UTF-8").mkString
//       logger.info(s"inContent: $inContent")
      logger.info(s"Copying template resource to ${file.toPath}")
      ResourceIO.copyFromResource(s"/${templateName}.conf.tmpl", file.toPath)
      logger.info(s"Copied template resource to ${file.toPath}")
      // try {
      //
      //   // copyFromResource(s"$templateName.conf.tmpl", file.toPath)
      //
      //   val resourceName =
      //   val target       = file.toPath
      //
      //   logger.info(s"Copying resource $resourceName to $target")
      //   // val in = getClass.getResourceAsStream(resourceName)
      //   //      val content = scala.io.Source.fromInputStream(in, "UTF-8").mkString
      //   val resourcePath = s"$resourceName"
      //   logger.info(s"Resource path: $resourcePath")
      //   val in            = getClass.getResourceAsStream(resourcePath)
      //   val resourcePath2 = s"/$resourceName"
      //   logger.info(s"Resource path: $resourcePath2")
      //   val in2 = getClass.getResourceAsStream(resourcePath2)
      //   logger.info(s"in: $in")
      //   logger.info(s"in2: $in2")
      //   val content = scala.io.Source.fromInputStream(in2, "UTF-8").mkString
      //   logger.info(s"WRITTING content")
      //   JFiles.write(target, content.getBytes(StandardCharsets.UTF_8))
      //   logger.info(s"WRITED")
      //   val resourcePath3 = s"/network_mainnet.conf.tmpl"
      //   logger.info(s"Resource path: $resourcePath3")
      //   val in3 = getClass.getResourceAsStream(resourcePath3)
      //   logger.info(s"in3: $in3")
      //   if (in != null) { in.close() }
      //   if (in2 != null) {
      //     in2.close()
      //   }
      //   if (in3 != null) {
      //     in3.close()
      //   }
      //
      // } catch {
      //   case e: Exception =>
      //     logger.error(s"Failed to copy template resource to $file", e)
      //     throw e
      // }
       logger.info(s"Created")
      if (!Env.isTestEnv) {
        logger.info(s"Setting $file to read-only")
        file.setWritable(false)
      }
    }

    logger.info(s"Using configuration file at $file \n")
    file
  }

  def copyFromResource(resourceName: String, target: Path): Unit = {
    logger.info(s"Copying resource $resourceName to $target")
    // val in = getClass.getResourceAsStream(resourceName)
    //      val content = scala.io.Source.fromInputStream(in, "UTF-8").mkString
    val resourcePath = s"$resourceName"
    logger.info(s"Resource path: $resourcePath")
    val in            = getClass.getResourceAsStream(resourcePath)
    val resourcePath2 = s"/$resourceName"
    logger.info(s"Resource path: $resourcePath2")
    val in2 = getClass.getResourceAsStream(resourcePath2)
    logger.info(s"in: $in")
    logger.info(s"in2: $in2")
    val content = scala.io.Source.fromInputStream(in2, "UTF-8").mkString
    logger.info(s"WRITTING content")
    JFiles.write(target, content.getBytes(StandardCharsets.UTF_8))
    logger.info(s"WRITED")
    val resourcePath3 = s"/network_mainnet.conf.tmpl"
    logger.info(s"Resource path: $resourcePath3")
    val in3 = getClass.getResourceAsStream(resourcePath3)
    logger.info(s"in3: $in3")
    if (in != null) { in.close() }
    if (in2 != null) {
      in2.close()
    }
    if (in3 != null) {
      in3.close()
    }
  }

  def getConfigFile(rootPath: Path, name: String): File = {
    val path = rootPath.resolve(s"$name.conf")
    Env.forProd(logger.info(s"Using $name configuration file at $path \n"))

    path.toFile
  }

  def getConfigNetwork(nodePath: Path, networkId: NetworkId, overwrite: Boolean): File =
    getConfigTemplate(nodePath, "network", s"network_${networkId.networkType}", overwrite)

  def getConfigSystem(env: Env, nodePath: Path, overwrite: Boolean): File = {
    getConfigTemplate(nodePath, "system", s"system_${env.name}", overwrite)
  }

  def getConfigUser(rootPath: Path): File = {
    val file = getConfigFile(rootPath, "user")
    if (!file.exists) { file.createNewFile }
    file
  }

  def parseConfigFile(file: File): Either[String, Config] =
    try {
      if (file.exists()) {
        Right(ConfigFactory.parseFile(file))
      } else {
        Right(ConfigFactory.empty())
      }
    } catch {
      case e: ConfigException =>
        Left(s"Cannot parse config file: $file, exception: $e")
    }

  def parseNetworkId(config: Config): Either[String, NetworkId] = {
    sys.env.get("ALEPHIUM_NETWORK_ID") match {
      case Some(id) =>
        NetworkId.from(id.toInt).toRight(s"Invalid chain id: $id")
      case None =>
        val keyPath = "alephium.network.network-id"
        if (!config.hasPath(keyPath)) {
          Right(NetworkId.AlephiumMainNet)
        } else {
          val id = config.getInt(keyPath)
          NetworkId.from(id).toRight(s"Invalid chain id: $id")
        }
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  def checkRootPath(rootPath: Path, networkId: NetworkId): Either[String, Unit] = {
    if (rootPath.toString.contains("mainnet") && networkId != NetworkId.AlephiumMainNet) {
      Left("The network is not mainnet, but the path contains mainnet")
    } else {
      Right(())
    }
  }

  def getNodePath(rootPath: Path, networkId: NetworkId): Path = {
    val nodePath = rootPath.resolve(networkId.nodeFolder)
    if (!JFiles.exists(nodePath)) {
      nodePath.toFile.mkdir()
    }
    nodePath
  }

  def updateGenesis(networkId: NetworkId, networkConfig: Config): Config = {
    if (networkId == NetworkId.AlephiumMainNet) {
      val genesisResource = this.getClass.getResource("/mainnet_genesis.conf")
      val genesis         = ConfigFactory.parseURL(genesisResource)
      networkConfig.withFallback(genesis)
    } else {
      networkConfig
    }
  }

  def parseConfig(env: Env, rootPath: Path, overwrite: Boolean, predefined: Config): Config = {
    val initialConfig = ConfigFactory.systemProperties().withFallback(predefined)
    val resultEither = for {
      userConfig <- parseConfigFile(getConfigUser(rootPath)).map(initialConfig.withFallback(_))
      networkId <- parseNetworkId(userConfig)
      _ <- checkRootPath(rootPath, networkId)
      nodePath = getNodePath(rootPath, networkId)
      systemConfig <- parseConfigFile(getConfigSystem(env, nodePath, overwrite))
      _networkConfig <- parseConfigFile(getConfigNetwork(nodePath, networkId, overwrite))
      networkConfig = updateGenesis(networkId, _networkConfig)
    } yield userConfig.withFallback(networkConfig.withFallback(systemConfig)).resolve()
    resultEither match {
      case Right(config) =>
        logger.info(s"CONFIG FILE GOOD")
        config
      case Left(error) =>
        logger.error(error)
        throw new RuntimeException(error)
    }
  }

  def parseConfigAndValidate(env: Env, rootPath: Path, overwrite: Boolean): Config = {
    val config = parseConfig(env, rootPath, overwrite, ConfigFactory.empty())
    if (!config.hasPath("alephium.discovery.bootstrap")) {
      logger.error(s"""|The bootstrap nodes are not defined!
                       |
                       |Please set the bootstrap nodes in $rootPath/user.conf and try again.
                       |
                       |Example:
                       |alephium.discovery.bootstrap = ["1.2.3.4:1234"] (or [] for test purpose)
                  """.stripMargin)
      sys.exit(1)
    } else {
      config
    }
  }

  def splitBalance(raw: String): Option[(LockupScript, U256)] = {
    val splitIndex = raw.indexOf(":")
    if (splitIndex == -1) {
      None
    } else {
      val left  = raw.take(splitIndex)
      val right = raw.drop(splitIndex + 1)
      for {
        bytestring   <- Hex.from(left)
        lockupScript <- deserialize[LockupScript](bytestring).toOption
        rawBalance   <- allCatch.opt(BigInt(right).underlying())
        balance      <- U256.from(rawBalance)
      } yield (lockupScript, balance)
    }
  }

  def loadBlockFlow(balances: AVector[Allocation])(implicit
      groupConfig: GroupConfig,
      consensusConfigs: ConsensusConfigs,
      networkConfig: NetworkConfig
  ): AVector[AVector[Block]] = {
    AVector.tabulate(groupConfig.groups, groupConfig.groups) { case (from, to) =>
      val transactions = if (from == to) {
        val balancesOI = balances
          .filter(_.address.lockupScript.groupIndex.value == from)
          .map(allocation =>
            (allocation.address.lockupScript, allocation.amount.value, allocation.lockDuration)
          )
        val transaction = Transaction.genesis(balancesOI, networkConfig.noPreMineProof)
        AVector(transaction)
      } else {
        AVector.empty[Transaction]
      }
      Block
        .genesis(ChainIndex.from(from, to).get, transactions)(groupConfig, consensusConfigs.mainnet)
    }
  }
}
