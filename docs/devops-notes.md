# CI/CD Pipeline Notes

## Overview
The CleanFlow mobile app is built and tested automatically using Azure Pipelines, which is connected to this GitHub repository.

## How it works
- **Trigger:** every push to the `master` branch starts a pipeline run.
- **Build agent:** an Ubuntu virtual machine hosted by Azure.
- **Steps:** Gradle runs the unit tests and builds the debug APK (`test assembleDebug`).
- **Results:** test results are published to Azure DevOps, under the Tests tab of each run.

## Task management
Work items are tracked on the Azure Boards (New, Active, Resolved, Closed). Commits reference work items with `#<id>`, for example `#13`.

## Known warnings
- The Gradle@2 task is deprecated and should be upgraded to Gradle@3.
- The `ubuntu-latest` image is moving to Ubuntu 26, so pinning to `ubuntu-24.04` is recommended.
