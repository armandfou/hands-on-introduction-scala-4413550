import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import scala.util.Using
import scala.io.Source
import scala.util.{Try, Success}
import Etl._
import Etl.EtlError.*
import pureconfig._
import pureconfig.generic.derivation.default._
import pureconfig.generic.derivation.EnumConfigReader
import org.scalatest.BeforeAndAfterAll
import java.io.FileWriter
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class EtlSpec extends AnyFreeSpec with Matchers with BeforeAndAfterAll {
  val stringInput = "src/test/resources/testInput.txt"
  val intInput = "src/test/resources/testInput2.txt"
  val jsonInput = "src/test/resources/testInput3.txt"
  val stringOutput = "src/test/resources/testOutput.txt"
  val intOutput = "src/test/resources/testOutput2.txt"
  val jsonOutput = "src/test/resources/testOutput3.txt"

  val inputJson: String =
    """
    |[
    |{"id": 1, "name":"bola", "age": 65},
    |{"id": 2, "name":"mary", "age": 15}
    |]
  """.stripMargin
  override def beforeAll() =
    writeTestInputFile(stringInput, "HELLO WORLD")
    writeTestInputFile(intInput, "0\n1\n2\n3\n4\n5\n")
    writeTestInputFile(jsonInput, inputJson)
  override def afterAll() =
    Files.delete(Path.of(stringInput))
    Files.delete(Path.of(intInput))
    Files.delete(Path.of(stringOutput))
    Files.delete(Path.of(intOutput))
    Files.delete(Path.of(jsonInput))
    Files.delete(Path.of(jsonOutput))

  "etl" - {
    "transforms a text file by making all the text lowercase and saves it to a new file" in {
      val expected = List("hello world")
      runIntegratedTest("string-test", Etl.StringImpl, expected)
    }
    "transforms a text file by doubling all integers and saves this to a new file" in {
      val expected = List("0", "2", "4", "6", "8", "10")
      runIntegratedTest("int-test", Etl.IntImpl, expected)
    }
    "transforms a json file by removing all users under the age of 18" in {
      val expected = List("User(1,bola,65)")
      runIntegratedTest("json-test", Etl.JsonImpl, expected)
    }
    "outputs an extract error if the input file path does not exist" in {
      val configWithErroneousInputFilePath =
        """
        |input-file-path = ""
        |output-file-path = "src/test/resources/testOutput.txt"
        |etl-impl = string-impl
        |""".stripMargin

      isExpectedError(
        configWithErroneousInputFilePath,
        Etl.StringImpl,
        EtlError.ExtractError
      )
    }
    "outputs a load error if the output file path does not exist" in {
      val configWithErroneousOutputFilePath =
        """
        |input-file-path = "src/test/resources/testInput2.txt"
        |output-file-path = ""
        |etl-impl = int-impl
        |""".stripMargin

      isExpectedError(
        configWithErroneousOutputFilePath,
        Etl.IntImpl,
        EtlError.LoadError
      )
    }
  }

  private def runIntegratedTest[A, B, C](
      configPath: String,
      etlImpl: Etl[A, B],
      expected: C
  ) =
    ConfigSource.default.at(configPath).load[EtlConfig] match
      case Left(_) => fail("there's been a problem loading the test config")
      case Right(config) =>
        etl(config, etlImpl)
        Using(Source.fromFile(config.outputFilePath))(
          _.getLines.toList
        ) shouldEqual Success(expected)
  end runIntegratedTest

  private def isExpectedError[A, B](
      config: String,
      impl: Etl[A, B],
      error: EtlError
  ) =
    ConfigSource
      .string(config)
      .load[EtlConfig] match
      case Left(_) => fail("there's been a problem loading the test config")
      case Right(config) =>
        etl(config, impl) shouldEqual Left(error)
  private def writeTestInputFile(path: String, contents: String) =
    val fileWriter = new FileWriter(new File(path))
    try {
      fileWriter.write(contents)
    } finally {
      fileWriter.close()
    }
}
