import io.circe.*
import io.circe.generic.semiauto.*

final case class User(id: Int, name: String, age: Int)

implicit val userDecoder: Decoder[User] = deriveDecoder[User]
implicit val userEncoder: Encoder[User] = deriveEncoder[User]