{
  description = "Provision a dev environment";

  inputs = {
    typelevel-nix.url = "github:typelevel/typelevel-nix";
    nixpkgs.follows = "typelevel-nix/nixpkgs";
    flake-utils.follows = "typelevel-nix/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils, typelevel-nix }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
          overlays = [ typelevel-nix.overlays.default ];
        };
      in
      {
        devShell = pkgs.devshell.mkShell {
          imports = [ typelevel-nix.typelevelShell ];
          name = "kind-projector";
          typelevelShell = {
            jdk.package = pkgs.jdk8;
          };
          commands = [{
            name = "back-publish";
            help = "back publishes a tag to Sonatype with a specific Scala version";
            command = "${./scripts/back-publish} $@";
          }];
        };
      }
    );
}
