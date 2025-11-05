## ase25-assignment02
### Simple mutational fuzzer

The fuzzer mutates a valid seed file using custom mutators and sends the generated input strings to the specified
command line program, monitoring their output and exit codes.

## Note for Windows users

If you're using WSL on Windows to work on the assignment, make sure that Git does not replace the Unix line endings with CRLF.
You can configure git to preserve the line endings from the remote repository:

```shell
git config --local core.autocrlf input
git config --local core.safecrlf true
```

Line ending normalization for text files is already enabled via [.gitattributes](https://github.com/se-ubt/ase24-assignment03/blob/main/.gitattributes).

If you're still getting errors caused by the line endings, you can try the following (after configuring Git as outlined above):

```shell
git add --update --renormalize
git checkout .
```

If that doesn't work, you can also exclicity convert all text files to use Unix line endings:

```shell
sudo apt-get install dos2unix
find . -type f -not -path "./.git/*" -exec dos2unix {} \;
git commit -a -m 'Convert line endings dos2unix'
```

## Run fuzzer

```shell
java Fuzzer.java "./html_parser_mac_universal" # or "html_parser_win_x86_64.exe" on Windows or "./html_parser_linux_x86_64" on Linux
```
