# UTT autocompletion

This useful tool is primarily designed to simplify typing
the same commands by autocompleting them

## Installation

Example install:

1. Make sure you are in ***qa-tool-automation-tool*** project
2. Run the following command in your terminal:
   `source cmd/install-bash-autocomplete`

## Usage

Type `cmd/utt` to start using autocomplete features

To list various *utt* phases:<br/>
`$cmd/utt [TAB][TAB]` (list all common predefined template phases)<br/>

To list various *build* phases:<br/>
`$cmd/utt build [TAB][TAB]` (list all common predefined build phases)<br/>

To list various *generate* phases:<br/>
`$cmd/utt generate [TAB][TAB]` (list all common predefined generate phases)<br/>

To complete just one word you just started typing, try to run, for instance:<br/>
`$cmd/utt g[TAB]` (complete to 'generate')