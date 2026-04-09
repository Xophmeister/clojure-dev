{
  description = "Clojure development environment";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
  };

  outputs =
    { self, nixpkgs }:
    let
      systems = [
        "x86_64-linux"
        "aarch64-linux"
        "x86_64-darwin"
        "aarch64-darwin"
      ];
      eachSystem = f: nixpkgs.lib.genAttrs systems (system: f nixpkgs.legacyPackages.${system});
    in
    {
      devShells = eachSystem (
        pkgs:
        let
          vim = pkgs.vim-full.customize {
            name = "vim";
            vimrcConfig = {
              packages.clojure = with pkgs.vimPlugins; {
                start = [
                  # REPL-driven development
                  vim-fireplace

                  # Linting & LSP
                  ale

                  # Structural editing
                  parinfer-rust
                  vim-sexp
                  vim-sexp-mappings-for-regular-people

                  # Rainbow parentheses
                  rainbow
                ];
              };

              customRC = ''
                " Source Home-Manager Vim config if available (set via shellHook)
                if !empty($HM_VIMRC) && filereadable($HM_VIMRC)
                  execute 'source' $HM_VIMRC
                endif

                " ── ALE: clj-kondo for linting, clojure-lsp for LSP ──
                function! ALEClojureProjectRoot(buffer) abort
                  for l:name in ['deps.edn', 'project.clj', 'build.boot']
                    let l:path = ale#path#FindNearestFile(a:buffer, l:name)
                    if !empty(l:path)
                      return fnamemodify(l:path, ':h')
                    endif
                  endfor
                  return ""
                endfunction

                call ale#linter#Define('clojure', {
                \   'name': 'clojure_lsp',
                \   'lsp': 'stdio',
                \   'executable': 'clojure-lsp',
                \   'command': '%e',
                \   'project_root': function('ALEClojureProjectRoot'),
                \})

                let g:ale_linters = extend(get(g:, 'ale_linters', {}), {'clojure': ['clj-kondo', 'clojure_lsp']})
                let g:ale_fixers = extend(get(g:, 'ale_fixers', {}), {'clojure': ['cljfmt']})
                let g:ale_clojure_cljfmt_options = '--remove-multiple-non-indenting-spaces'

                " ── Rainbow parentheses ──
                let g:rainbow_active = 1
              '';
            };
          };
        in
        {
          default = pkgs.mkShell {
            packages = with pkgs; [
              # Clojure runs on the JVM
              jdk

              # Core tooling
              clojure # clj / clojure CLI (deps.edn)
              leiningen # alternative build tool (project.clj)
              babashka # fast Clojure scripting / task runner

              # Editor support
              clojure-lsp
              clj-kondo # linter
              cljfmt # formatter (also bundled in clojure-lsp)

              # Vim with Clojure plugins
              vim
            ];

            shellHook = ''
              # Extract the Home-Manager vimrc path so our wrapper can source it
              if [ -f "$HOME/.nix-profile/bin/vim" ]; then
                HM_VIMRC=$(grep -oP "(?<=-u ').*(?=')" "$HOME/.nix-profile/bin/vim" 2>/dev/null || true)
                export HM_VIMRC
              fi

              echo "Clojure dev environment loaded."
              echo "  Start a REPL: clj -M:repl / nix run .#repl"
              echo "  Then open vim; fireplace auto-connects via .nrepl-port"
            '';
          };
        }
      );

      apps = eachSystem (pkgs: {
        repl = {
          type = "app";
          program = "${
            pkgs.writeShellApplication {
              name = "clj-repl";
              runtimeInputs = [
                pkgs.clojure
                pkgs.jdk
              ];
              text = "clj -M:repl";
            }
          }/bin/clj-repl";
        };
      });
    };
}
