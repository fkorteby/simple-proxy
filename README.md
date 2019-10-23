# Simple Proxy supporting fail-overs scenarios :
This is a forwarding Proxy using the basic Java socket and operating with multi threaded technics.

I created this proxy to handle fail-overs scenarios in clustered database environment. But it can be used in any failed-over context.

Some key points about simple proxy :

- It's **fast** : working under TCP/IP level.
- It's **simple** : can be customized with your own logic.
- It's **strong** : supporting fail-over in clustered environment.
- It's **multi-platforms** : needs just installed JRE to work.

## Usage :

### Normal start command :
> java -jar simple-proxy.jar <local-port>:<time-out> <host1>:<remote-port1> <host2>:<remote-port2> ...

### Configure as service in windows :
Using the [NSSM - the Non-Sucking Service Manager](https://nssm.cc/) you can create a windows based service like this :
> `nssm remove Simple-Proxy confirm`
> `nssm install Simple-Proxy "C:\Windows\System32\cmd.exe" "%cd%\run-proxy.bat"`
> `nssm set Simple-Proxy AppDirectory "%cd%"`
> `nssm set Simple-Proxy AppParameters /c "%cd%\run-proxy.bat"`
> `nssm set Simple-Proxy AppStderr "%cd%\proxy-console.err"`
> `nssm set Simple-Proxy AppStdout "%cd%\proxy-console.out"`

Note : This service will create two log files, one will contains the logging event and the second will contains errors.

The file **run-proxy.bat** will be something like this :
> `java -jar simple-proxy.jar <local-port>:<time-out> <host1>:<remote-port1> <host2>:<remote-port2> ...`

Note : All files must be in the same directory.

## Download :

