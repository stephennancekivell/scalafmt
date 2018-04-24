package org.scalafmt.config

import metaconfig._

/**
  * Configuration related ot multi-line formatting.
  */
@DeriveConfDecoder
case class VerticalMultiline(
    atDefinitionSite: Boolean = false,
    arityThreshold: Int = 100,
    newlineBeforeImplicitKW: Boolean = false,
    newlineAfterImplicitKW: Boolean = false,
    newlineAfterOpenParen: Boolean = false,
    excludeDanglingParens: List[DanglingExclude] = List(
      DanglingExclude.`class`,
      DanglingExclude.`trait`
    )
)

sealed abstract class DanglingExclude

object DanglingExclude {
  case object `class` extends DanglingExclude
  case object `trait` extends DanglingExclude

  implicit val danglingExcludeReader: ConfDecoder[DanglingExclude] =
    ReaderUtil.oneOf[DanglingExclude](`class`, `trait`)
}
