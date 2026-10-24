# i3statusviewer
util programs for i3status.
This bundles multiple programs like busybox.
So, please make softlinks before running.

## i3statusviewer-viewer
It display the outputs of i3status(or compatible software) in table view.
It is useful for checking outputs over ssh.

```
# check i3status output
$ ./i3statusviewer-viewer

# or check other i3status like commands
$ ./i3statusviewer-viewer -c i3status-rs
```

## i3statusviewer-translator
It translate i3status output into JSONL style, or vice versa.

```
# translate to JSONL
$ echo '
>  {"version": 1}
>  [
>  [{"hoge": "fuga"}],
>  [{"piyo": "piyopiyo"}],
>  ' | i3statusviewer-translator decode
{"version": 1}
[{"hoge": "fuga"}]
[{"piyo": "piyopiyo"}]


# translate from JSONL
$ echo '
> {"version": 1}
> [{"hoge": "fuga"}]
> [{"piyo": "piyopiyo"}]
> ' | i3statusviewer-translator encode
{"version": 1}
[
[{"hoge": "fuga"}],
[{"piyo": "piyopiyo"}],
```

