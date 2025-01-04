import pureconfig.*
import pureconfig.generic.derivation.default.*
import pureconfig.generic.derivation.EnumConfigReader

final case class EtlConfig(
    inputFilePath: String,
    outputFilePath: String,
    etlImpl: EtlImpl
) derives ConfigReader

enum EtlImpl derives EnumConfigReader:
  case StringImpl, IntImpl
