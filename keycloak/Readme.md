# Setup keycloak realm for dev

1. Create realm with name `mostserver`
2. Under Realm Settings -> Actions -> Partial import
    1. Import `realm-users.json`
    2. Import `realm-export.json`
        * Select all resources
        * Set `overwrite` on existing resources
