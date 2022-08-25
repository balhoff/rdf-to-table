# rdf-to-table
Convert RDF graph to simple integer-based edge table.

## Running

Provide an RDF file as `input-file`. The format is guessed from the file extension. The input can be zipped (e.g., `mydata.ttl.gz`) Specify files to write to for the edges table (`edges-file`), node labels table (`nodes-file`), and edge labels table (`predicates-file`).

__Note__: only _IRI nodes_ are output. Triples containing blank nodes or literals are dropped.

```
rdf-to-table --help

Usage: [options]
  --usage  <bool>
        Print usage and exit
  --help | -h  <bool>
        Print help message and exit
  --input-file  <path/to/file>
  --edges-file  <path/to/file>
  --nodes-file  <path/to/file>
  --predicates-file  <path/to/file>
  ```
  
