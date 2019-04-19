saml_response=$1
result_file=$2

urldecode() { : "${*//+/ }"; echo -e "${_//%/\\x}"; }
decode=$(urldecode $(cat $saml_response))
echo $decode | base64 --decode > $2