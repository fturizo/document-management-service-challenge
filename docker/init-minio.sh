#!/bin/bash
set -e

until mc alias set local http://minio:9000 "${MINIO_ROOT_USER}" "${MINIO_ROOT_PASSWORD}" >/dev/null 2>&1; do
  sleep 1
done
echo "Minio alias set."

if mc ls local/"$BUCKET_NAME" >/dev/null 2>&1; then
  echo "Bucket '$BUCKET_NAME' already exists — skipping initialization."
  exit 0
fi

mc mb local/"$BUCKET_NAME"
echo "Created bucket '$BUCKET_NAME'."

cat >/tmp/"$BUCKET_NAME"-rw.json <<'EOF'
{
  "Version": "2012-10-17",
  "Statement": [
    { "Effect": "Allow",
      "Action": ["s3:GetBucketLocation","s3:ListBucket"],
      "Resource": ["arn:aws:s3:::${BUCKET_NAME}"]
    },
    { "Effect": "Allow",
      "Action": ["s3:GetObject","s3:PutObject","s3:DeleteObject"],
      "Resource": ["arn:aws:s3:::${BUCKET_NAME}/*"]
    }
  ]
}
EOF

mc admin policy create local "$BUCKET_NAME"-rw /tmp/"$BUCKET_NAME"-rw.json
echo "Policy created."

mc admin accesskey create local --access-key "$ACCESS_KEY" --secret-key "$SECRET_KEY"
echo "Access key '$ACCESS_KEY' created."

mc admin policy attach local --user "$ACCESS_KEY" "$BUCKET_NAME"-rw
echo "Policy attached."

echo "MinIO Preparation complete."
