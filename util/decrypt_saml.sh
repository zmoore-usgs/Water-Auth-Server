# Most of this taken from https://security.stackexchange.com/questions/76567
encoded_key=$1
encoded_data=$2
saml_key=$3
result_path=$4

# Decode Data
cat $encoded_key | base64 -d -i > key.bin
cat $encoded_data | base64 -d -i > data.bin

# Decrypt AES Key
openssl rsautl -decrypt -inkey $saml_key -oaep -in key.bin -out key.dec

# Decrypt Data
openssl enc -nopad -d -aes-256-cbc -in data.bin -out decrypted.bin -K $(xxd -p -c 256 key.dec) -iv "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"

# Remove Bit Padding
dd if=decrypted.bin of=final.bin bs=16 skip=1
trail_bits=$(printf '%d' "'$(tail -c 1 final.bin)'")
head -c -$trail_bits final.bin > $result_path

# Cleanup
rm -rf key.bin data.bin key.dec decrypted.bin final.bin