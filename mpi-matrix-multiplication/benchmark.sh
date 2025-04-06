#!/bin/bash

# run from the script folder, executables are expected in ./Debug (override with BIN_DIR)
cd "$(dirname "$0")" || exit 1
BIN_DIR="${BIN_DIR:-./Debug}"

MATRIX_SIZES=(64 128 256 512 1024 2048)
NUM_WORKERS=(2 4 8)

for size in "${MATRIX_SIZES[@]}"
do
  echo "========== Matrix Size: ${size}×${size} =========="

  echo "[Sequential]"
  "$BIN_DIR"/sequential "$size" "$size" "$size"

  for nw in "${NUM_WORKERS[@]}"
  do
    np=$((nw + 1))
    echo "=== Workers: $nw ==="

    echo "[Blocking]"
    mpiexec -np "$np" "$BIN_DIR"/blocking "$size" "$size" "$size"

    echo "[Non-blocking]"
    mpiexec -np "$np" "$BIN_DIR"/non-blocking "$size" "$size" "$size"

    echo "[Collective]"
    mpiexec -np "$nw" "$BIN_DIR"/collective "$size" "$size" "$size"

    echo ""
  done
done

# keep the window open when started by double-click on Windows
if [ -t 0 ]; then
  read -p "Press any key to close..."
fi
