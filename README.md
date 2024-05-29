# Table editor plugin

<!-- Plugin description -->
This plugin adds formulas support to CSV tables.
<!-- Plugin description end -->

## How to run:
Run gradle `runIde` task, e.g. on unix environment:
  ```shell
  cd path/to/table-editor-plugin
  ./gradlew runIde
  ```
Open any project with and any CSV file

## Operations supported:
- plus: `+`
- minus (both unary and binary): `-`
- multiply: `*`
- division: `/`
- square root: `sqrt()` (lowercase)

## Formula format:
- Braces `(`, `)` are allowed
- Formulas begin from `=`
- Cells are written like `COLROW`, example: `A2` (uppercase)
- Numbers
