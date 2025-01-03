import scala.util.Using
import scala.io.Source
import java.io.File
import java.io.FileWriter

@main def run: Unit =
  val input: String = ""
  val output: String = ""
  etl(input, output)

def etl(inputFilePath: String, outputFilePath: String): Unit =
  val extracted = extract(inputFilePath)
  val transformed = transform(extracted)
  load(transformed, outputFilePath)
end etl

def extract(input: String): List[String] =
  Using.resource(Source.fromFile(input))(res => res.getLines.toList)
def transform(data: List[String]): List[String] =
  data.map(line => line.toLowerCase)
def load(
    data: List[String],
    output: String = "src/main/resources/output.txt"
): Unit =
  val file = new File(output)
  val fileWriter = new FileWriter(file)
  fileWriter.write(data.mkString("\n"))
  fileWriter.close()
end load
