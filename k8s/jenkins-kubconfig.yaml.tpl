apiVersion: v1
clusters:
  - cluster:
      certificate-authority-data: ${CERTIFICATE_AUTH}
      server: ${URL_SERVER_KUB}
    name: kind-avaliator
contexts:
  - context:
      cluster: kind-avaliator
      user: kind-avaliator
    name: kind-avaliator
current-context: kind-avaliator
kind: Config
preferences: {}
users:
  - name: kind-avaliator
    user:
      client-certificate-data: ${CLIENT_CERTIFICATE}
      client-key-data: ${CLIENT_KEY}
