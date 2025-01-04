import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import scala.util.Using
import scala.io.Source
import scala.util.{Try, Success}
import Etl._
import Etl.EtlError.*
import pureconfig.ConfigSource

class EtlSpec extends AnyFreeSpec with Matchers {

  "etl" - {
    "transforms a text file by making all the text lowercase and saves it to a new file" in {

      val expectedFileContents = List("hello world")
      runIntegratedTest("string-test", Etl.StringImpl, expectedFileContents)
    }
    "transforms a text file by doubling all integers and saves this to a new file" in {
      val expectedFileContents = List("0", "2", "4", "6", "8", "10")
      runIntegratedTest("int-test", Etl.IntImpl, expectedFileContents)
    }
    "outputs an extract error if the input file path does not exist" in {
      val configWithErroneousInputFilePath =
        """
        | input-file-path = ""
        | output-file-path = "src/test/resources/testOutput.txt"
        | etl-impl = string-impl
      """.stripMargin
      isExpectedError(
        configWithErroneousInputFilePath,
        Etl.IntImpl,
        ExtractError
      )

    }
    "outputs a load error if the output file path does not exist" in {
      val configWithErroneousOutputFilePath =
        """
        | input-file-path = "src/test/resources/testInput2.txt"
        | output-file-path = ""
        | etl-impl = int-impl
      """.stripMargin
      isExpectedError(
        configWithErroneousOutputFilePath,
        Etl.IntImpl,
        LoadError
      )
    }
  }

  private def readFile(filePath: String): Try[List[String]] =
    Using(Source.fromFile(filePath))(_.getLines.toList)
  private def runIntegratedTest[A, B, C](
      configPath: String,
      etlImpl: Etl[A, B],
      expected: C
  ) =
    ConfigSource.default.at(configPath).load[EtlConfig] match {
      case Left(_) => fail(s"there's a problem loading the test config")
      case Right(config) =>
        etl(config, etlImpl)
        Using(Source.fromFile(config.outputFilePath))(
          _.getLines.toList
        ) shouldEqual Success(expected)
    }
  end runIntegratedTest
  private def isExpectedError[A, B](
      config: String,
      impl: Etl[A, B],
      error: EtlError
  ) =
    ConfigSource.string(config).load[EtlConfig] match
      case Left(_)       => fail(s"there's a problem loading the test config")
      case Right(config) => etl(config, impl) shouldEqual Left(error)

}
