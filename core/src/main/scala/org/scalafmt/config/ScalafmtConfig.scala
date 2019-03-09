package org.scalafmt.config

import scala.io.Codec
import scala.util.control.NonFatal
import scala.util.matching.Regex

import metaconfig.ConfigReader
import metaconfig.Reader
import org.scalafmt.util.ValidationOps

/** Configuration options for scalafmt.
  *
  * @param version The version of scalafmt to use for this project. Currently not used,
  *                the plan is to use this field for the IntelliJ+sbt integrations.
  * @param maxColumn Column limit, any formatting exceeding this field is
  *                  penalized heavily.
  * @param docstrings Several options:
  *                   - ScalaDoc: format as Scala docs
  *                   - JavaDocs: format as Java docs
  *                   - preserve: keep existing formatting
  * @param assumeStandardLibraryStripMargin If true, the margin character | is treated
  *                                as the new indentation in multiline strings
  *                                ending with `.stripMargin`.
  * @param danglingParentheses If true
  *                            AND @binPackArguments is true
  *                            AND @configStyleArguments is false, then this
  *
  *                            function(
  *                                longerArg1,
  *                                longerArg3)
  *
  *                            is formatted like this
  *
  *                            function(
  *                                longerArg1,
  *                                longerArg3
  *                            )
  * @param unindentTopLevelOperators If true, allows no indentation on infix operators
  *                                  in non-top-level functions. For example,
  *
  *                                  function(
  *                                      a &&
  *                                      b
  *                                  )
  *
  *                                  If false, only allows 0 space indentation for
  *                                  top-level statements
  *
  *                                  a &&
  *                                  b
  *                                  function(
  *                                      a &&
  *                                        b
  *                                  )
  *
  *                                  Context: https://github.com/scala-js/scala-js/blob/master/CODINGSTYLE.md#long-expressions-with-binary-operators
  * @param rewriteTokens Map of tokens to rewrite. For example, Map("⇒" -> "=>")
  *                      will rewrite unicode arrows to regular ascii arrows.
  * @param importSelectors Controls formatting of import selectors with multiple names from the
  *                        same package;
  *                        If [[org.scalafmt.config.ImportSelectors.binPack]], import selectors are
  *                        arranged to fit within the maximum line width
  *                        If [[org.scalafmt.config.ImportSelectors.noBinPack]], import selectors
  *                        are broken to one per line
  *                        If [[org.scalafmt.config.ImportSelectors.singleLine]], import selectors
  *                        are kept on a single line
  *                        The default setting is currently `noBinPack`.
  * @param poorMansTrailingCommasInConfigStyle (experimental, may be removed)
  *                                      If true, formats config style like this:
  *
  *                                      function(
  *                                        a
  *                                        , b
  *                                        , c
  *                                      )
  * @param indentYieldKeyword If true, indents `yield` by two spaces
  *                           for (i <- j)
  *                             yield banana
  *                           If false, treats `yield` like `else`
  *                           for (i <- j)
  *                           yield banana
  * @param lineEndings If [[LineEndings.unix]], output will include only unix line endings
  *                    If [[LineEndings.windows]], output will include only windows line endings
  *                    If [[LineEndings.preserve]], output will include endings included in original
  *                    file (windows if there was at least one windows line ending, unix if there
  *                    was zero occurrences of windows line endings)
  * @param includeCurlyBraceInSelectChains
  *  If true, includes curly brace applications in select chains/pipelines.
  *  {{{
  *    // If true
  *    List(1)
  *      .map { x =>
  *        x + 2
  *      }
  *      .filter(_ > 2)
  *    // If false
  *    List(1).map { x =>
  *        x + 2
  *    }.filter(_ > 2)
  *  }}}
  * @param verticalMultilineAtDefinitionSite If true, reformat multi-line function definitions in
  *  the following way
  * {{{
  *   def format(
  *       code: String,
  *       age: Int
  *     )(implicit ev: Parser,
  *       c: Context
  *     ): String
  * )
  * }}}
  *
  *  All parameters are on their on line indented by four (4), seperation between
  *  parament groups are indented by two (2). ReturnType is on its own line at
  *  then end. This will only trigger if the function would go over
  *  [[maxColumn]]. If a multi-line funcion can fit in a single line, it will
  *  make it so. Note that this setting ignores continuation.defnSite,
  *  [[binPack.defnSite]], and [[align.openParenDefnSite]].
  */
@ConfigReader
case class ScalafmtConfig(
    version: String = org.scalafmt.Versions.stable,
    maxColumn: Int = 80,
    docstrings: Docstrings = Docstrings.ScalaDoc,
    optIn: OptIn = OptIn(),
    binPack: BinPack = BinPack(),
    continuationIndent: ContinuationIndent = ContinuationIndent(),
    align: Align = Align(),
    spaces: Spaces = Spaces(),
    lineEndings: LineEndings = LineEndings.unix,
    rewriteTokens: Map[String, String] = Map.empty[String, String],
    rewrite: RewriteSettings = RewriteSettings(),
    indentOperator: IndentOperator = IndentOperator(),
    newlines: Newlines = Newlines(),
    runner: ScalafmtRunner = ScalafmtRunner.default,
    indent: IndentConfig = IndentConfig(),
    // Settings which belong to no group
    indentYieldKeyword: Boolean = true,
    @metaconfig.ExtraName("binPackImportSelectors") importSelectors: ImportSelectors =
      ImportSelectors.noBinPack,
    unindentTopLevelOperators: Boolean = false,
    includeCurlyBraceInSelectChains: Boolean = true,
    assumeStandardLibraryStripMargin: Boolean = false,
    danglingParentheses: Boolean = false,
    poorMansTrailingCommasInConfigStyle: Boolean = false,
    bestEffortInDeeplyNestedCode: Boolean = false,
    verticalMultilineAtDefinitionSite: Boolean = false,
    onTestFailure: String = "",
    encoding: Codec = "UTF-8",
    project: ProjectFiles = ProjectFiles(),
    literalCase: LiteralCase = LiteralCase()
) {

  def reformatDocstrings: Boolean = docstrings != Docstrings.preserve
  def scalaDocs: Boolean = docstrings == Docstrings.ScalaDoc

  implicit val runnerReader: Reader[ScalafmtRunner] =
    runner.reader
  implicit val indentConfigReader: Reader[IndentConfig] =
    indent.reader
  implicit val contIndentReader: Reader[ContinuationIndent] =
    continuationIndent.reader
  implicit val indentReader: Reader[IndentOperator] =
    ScalafmtConfig.indentReader
  implicit val binPackReader: Reader[BinPack] = binPack.reader
  implicit val alignReader: Reader[Align] = align.reader
  implicit val lineEndingReader: Reader[LineEndings] = LineEndings.reader
  implicit val spacesReader: Reader[Spaces] = spaces.reader
  implicit val docstringsReader: Reader[Docstrings] = Docstrings.reader
  implicit val literalCaseReader: Reader[LiteralCase] = literalCase.reader
  implicit val rewriteReader: Reader[RewriteSettings] = rewrite.reader
  implicit val optInReader: Reader[OptIn] = optIn.reader
  implicit val newlinesReader: Reader[Newlines] = newlines.reader
  implicit val projectReader: Reader[ProjectFiles] = project.reader
  implicit val importSelectorsReader: Reader[ImportSelectors] =
    ImportSelectors.backwardsCompatibleReader
  implicit val codecReader: Reader[Codec] = ScalafmtConfig.codecReader

  lazy val alignMap: Map[String, Regex] =
    align.tokens.map(x => x.code -> x.owner.r).toMap
  ValidationOps.assertNonNegative(
    continuationIndent.callSite,
    continuationIndent.defnSite
  )
}

object ScalafmtConfig extends Settings
