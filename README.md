# Sorting Algorithm Performance Evaluator (IC4302 - Programming III)

Java Swing application that loads a CSV file, lets the user pick a numeric column, sorts it with
Insertion, Shell, Merge, Quick and Heap Sort, measures the execution time of each and highlights
the fastest algorithm. No external libraries - plain JDK (11 or newer).

## Run in IntelliJ IDEA
1. File > Open... and choose the `SortingPerformanceEvaluator` folder (or File > New > Project from Existing Sources).
2. File > Project Structure > Project: set SDK to JDK 11 or newer.
3. Project Structure > Modules > Sources: make sure `src` is marked blue (Sources Root).
4. Open `src/com/sortperf/Main.java` and press the green Run arrow next to `main`.
5. Click **Upload CSV...** and choose a file from the `data/` folder.

Optional correctness check: run `src/com/sortperf/tools/SortSelfTest.java`.

## Package overview
| Package   | Responsibility |
|-----------|----------------|
| `io`      | CSV reading and validation (`CsvLoader`, `CsvLoadException`) |
| `model`   | Data holders (`Dataset`, `NumericColumn`, `SortResult`) |
| `sort`    | `SortingAlgorithm` interface + the five algorithms |
| `service` | `PerformanceEvaluator` - timing, verification, ranking |
| `ui`      | Swing GUI (`MainFrame`, `BarChartPanel`) |
| `tools`   | `SortSelfTest` - console correctness test |

## Sample data (`data/`)
- `sample_employees.csv` - 1,000 rows, a few blank scores
- `sample_sales_20000.csv` - 20,000 rows
- `edge_cases/` - empty file, header-only, no numeric column, mixed/blank/quoted values, wrong extension
