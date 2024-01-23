#!/bin/bash
set -e

# See for gen version and region support https://cloud.google.com/functions/docs/locations#tier_1_pricing
# Please create `set-env-vars.sh` file with next variables defined
# export BUCKET_NAME=
# export SERVICE_ACCOUNT=
# export REGION=
# export GCP_PROJECT_ID=

source set-env-vars.sh

if [[ -z "${BUCKET_NAME}" || -z "${SERVICE_ACCOUNT}" || -z "${REGION}" || -z "${GCP_PROJECT_ID}" ]]; then
  echo "One or more variables are undefined"
  exit 1
fi

gcloud functions deploy thumbnail-generator \
--region="${REGION}" \
--entry-point ovh.kg4.pkravchenko.ThumbnailGenFunction \
--gen2 \
--runtime java17 \
--timeout=120s \
--service-account="${SERVICE_ACCOUNT}" \
--source target/deploy \
--memory=256Mi \
--max-instances=4 \
--set-env-vars="gcp.project.id=${GCP_PROJECT_ID}" \
--trigger-event-filters="type=google.cloud.storage.object.v1.finalized" \
--trigger-event-filters="bucket=${BUCKET_NAME}"
