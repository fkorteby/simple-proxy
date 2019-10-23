nssm remove Simple-Proxy confirm
nssm install Simple-Proxy "C:\Windows\System32\cmd.exe" "%cd%\run-proxy.bat"
nssm set Simple-Proxy AppDirectory "%cd%"
nssm set Simple-Proxy AppParameters /c "%cd%\run-proxy.bat"
nssm set Simple-Proxy AppStderr "%cd%\proxy-console.err"
nssm set Simple-Proxy AppStdout "%cd%\proxy-console.out"