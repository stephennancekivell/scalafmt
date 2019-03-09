package org.scalafmt.config

import metaconfig.ConfigReader

@ConfigReader
case class LiteralCase(
    longLowerCase: Boolean = false,
    longUpperCase: Boolean = false,
    doubleLowerCase: Boolean = false,
    doubleUpperCase: Boolean = false,
    floatLowerCase: Boolean = false,
    floatUpperCase: Boolean = false
)
