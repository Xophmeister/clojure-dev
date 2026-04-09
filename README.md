# Clojure-in-Vim cheatsheet

## REPL (`vim-fireplace`)

 | Keys         | What it does                                                                |
 | ------------ | --------------------------------------------------------------------------- |
 | `cpp`        | Evaluate the innermost form under the cursor                                |
 | `cp<motion>` | Evaluate a motion (e.g., `cpab` for outer form, `cpip` for paragraph)       |
 | `cqp`        | Open a REPL prompt to type and evaluate an expression                       |
 | `cqq`        | Re-run the last REPL prompt expression                                      |
 | `cqc`        | Evaluate the form under cursor and put result in a REPL prompt for chaining |
 | `K`          | Look up docs for the symbol under cursor                                    |
 | `[d`         | Show source of the symbol under cursor                                      |
 | `[<C-d>`     | Jump to definition of the symbol under cursor                               |
 | `:Require`   | Reload the current namespace                                                |
 | `:Require!`  | Reload all namespaces                                                       |

## ALE (linting & LSP)

 | Keys/Command              | What it does                                |
 |---------------------------|---------------------------------------------|
 | `gd`                      | Go to definition                            |
 | `gr`                      | Rename symbol                               |
 | `:ALEHover`               | Show type/docs for symbol under cursor      |
 | `:ALEFindReferences`      | Find all usages of a symbol                 |
 | `:ALECodeAction`          | Apply LSP code actions (add requires, etc.) |
 | `:ALESymbolSearch <name>` | Search for symbols across the project       |

## Structural editing (`vim-sexp`)

 | Keys            | What it does                                                 |
 |-----------------|--------------------------------------------------------------|
 | `>)`            | Slurp right — pull the next element into the current form    |
 | `<)`            | Barf right — push the last element out of the current form   |
 | `<(`            | Slurp left — pull the previous element into the current form |
 | `>(`            | Barf left — push the first element out of the current form   |
 | `dsf`           | Splice — remove surrounding form, keep contents              |
 | `cse(` / `cse)` | Surround element in parens                                   |
 | `cseb` / `cseB` | Surround element in `[]` / `{}`                              |

## Parinfer

Parinfer works automatically: just type and it manages closing delimiters
for you. It infers the structure from your indentation. If it gets confused,
`:ParinferOff` to disable temporarily.

## CLI tools

 | Command                | What it does                          |
 |------------------------|---------------------------------------|
 | `clj -M:repl`          | Start nREPL (needed for fireplace)    |
 | `clj`                  | Start a basic Clojure REPL (no nREPL) |
 | `bb`                   | Run Babashka for fast scripting       |
 | `clj-kondo --lint src` | Lint from command line                |
 | `cljfmt fix src`       | Format all source files               |

## Typical workflow

1. Tab 1: `nix run .#repl` or, inside the Nix devshell,`clj -M:repl`
2. Tab 2: `vim src/whatever.clj` inside the Nix devshell
3. Write code, `cpp` to evaluate, iterate
4. `:Require` when you change a namespace and want to reload it
