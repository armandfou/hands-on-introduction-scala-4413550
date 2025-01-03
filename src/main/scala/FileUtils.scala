import java.io.{File, FileWriter}
import scala.io.Source
import scala.util.Using
import Etl.EtlError
import Etl.*
import scala.util.Try

object FileUtils:
  def extract(input: String): Either[EtlError, List[String]] =
    try Right(Using.resource(Source.fromFile(input))(_.getLines.toList))
    catch case e: Exception => Left(EtlError.ExtractError)
  end extract

  def load[A](data: List[A], output: String): Either[EtlError, Unit] =
    Try {
      val file = new File(output)
      val fileWriter = new FileWriter(file)
      fileWriter.write(data.mkString("\n"))
      fileWriter.close()
    }.toEither.left.map(_ => EtlError.LoadError)
  end load
end FileUtils
