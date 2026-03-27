apiVersion: v1
kind: Secret
metadata:
  name: postgres-credentials
  namespace: avaliator
  labels:
    app.kubernetes.io/component: database
    app.kubernetes.io/part-of: avaliator
type: Opaque
stringData:
  POSTGRES_USER: ${POSTGRES_USER}
  POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
  POSTGRES_DB: ${POSTGRES_DB}
