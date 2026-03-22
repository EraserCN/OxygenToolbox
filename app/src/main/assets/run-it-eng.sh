#!/system/bin/sh
SLOT=$(getprop ro.boot.slot_suffix 2>/dev/null)
PART="/dev/block/by-name/xbl_config${SLOT}"
IMAGE_PATH="/data/local/tmp/xbl_config${SLOT}.img"

su -c "dd if=$PART of=${IMAGE_PATH} bs=4096" &>/dev/null

SCRIPT_DIR=$(dirname "$0")
BIN="$SCRIPT_DIR/arbscan"
TMP_BIN="/data/local/tmp/arbscan"

su -c "cp \"$BIN\" \"$TMP_BIN\""
su -c "chmod +x \"$TMP_BIN\""

FULL_OUTPUT=$(echo N | su -c "$TMP_BIN" "$IMAGE_PATH" 2>&1 | grep "ARB Index")
IDX=$(echo "$FULL_OUTPUT" | awk -F: '/ARB Index/{gsub(/ /,"");print $2}')

su -c rm -f "$IMAGE_PATH"
su -c rm -f "$TMP_BIN"

if [ "$IDX" = "0" ]; then
    echo "Anti-Rollback Disabled"
else
    echo "Anti-Rollback Enabled"
fi
