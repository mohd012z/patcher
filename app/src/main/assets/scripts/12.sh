main_func() {
local F="$@";
local base_name="${F##*/}";
local j_path="${F%$base_name}";
local util_dir="${0%/*}/bin";
local sign_keypm="${util_dir}/keys/releasekey.x509.pem";
local sign_keypk="${util_dir}/keys/releasekey.pk8";
local un_bb="${util_dir}/busybox";
if [ -n "$j_path" ]; then
cd "$j_path" || ab_ort "Can not 'cd' to
    \"$j_path\"!
";
fi
local n_keypm="${F%.x509.pem}";
local n_keypm="${n_keypm%.pk8}";
[ "$F" = "$n_keypm" ] && ab_ort "File
    \"${F}\"
  is not a signing key file!";
local start_at="$(date +%H:%M:%S)";
setspan_green "
  My sign working...
";
local n_keypk="${n_keypm}.pk8";
local n_keypm="${n_keypm}.x509.pem";
[ -f "$n_keypm" ] || ab_ort "File
    \"${n_keypm}\"
  is not exist!

  It is not possible to sign packages with the .pk8 file only!";
[ -f "$n_keypk" ] || ab_ort "File
    \"${n_keypk}\"
  is not exist!

  It is not possible to sign packages with the .x509.pem file only!";
setspan_blue "  Selected files:";
echo "    \"${n_keypm}\"
    \"${n_keypk}\"";
setspan_blue "  Replacing signing key files...";
"${un_bb}" cp -f "$n_keypm" "$sign_keypm";
"${un_bb}" cp -f "$n_keypk" "$sign_keypk";
setspan_blue "
  Done!
";
setspan_green "  Results:";
echo "      Start time: ${start_at}
        End time: $(date +%H:%M:%S)";
exit
}
[ -f "${0%/*}/bin/utils" ] && { . "${0%/*}/bin/utils"; } || {
setspan_red() {
echo "$@";
}
setspan_green() {
echo "$@";
}
setspan_blue() {
echo "$@";
}
}
ab_ort() {
local tx_t="$@";
setspan_red "
  ${tx_t}
";
exit
}
[ -z "$@" ] && ab_ort "No arguments passed!";
[ "$(echo -n "$@")" = "reset" ] && {
"${0%/*}/bin/busybox" sed -i 's|\(\"args\":\"\)[^,][^,]*\",|\1\",|g' "${0%.*}.prop";
setspan_blue "
  Writing standart \"releasekey.pk8\"...";
echo -n "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCqaA3n7drxmOZ+uFaGa/n/e+LuEJt3oqvsXa3R0V+GqC1R3y3y4iP0hLPohZpoCZjh9YEa7lS8+xRLb1akQE4diIQ1qOcyyrIGknNVgEcNT1IZfd9kd23NTsGxJ6QQzkGL9pNcyyYnNsy0KyQ/41EryT7+oteWVSirQOyd0cvb9T2TxQwp8SOWAwtBSuE3WOvZWSHS6wh6GuvmZHv/zUUfflymNm6CDs708NwOiNW8VfsyhBbKZIB7CLaVZA1VKoFJ0M/bejuIbhk0Iu8t4ZS62y6cOXOJIajWRquiOPkWWVoFFrfP/Uhim6zefaNqIYIFZerghkc0iLvlwuye1Q1vAgMBAAECggEAdIn9GKSem6PJ3tgKtJubuZHsPIu3BzaFBX17pobeIaVaeSd8sSLfHBa1Q0w7sPL/T2krBlQAN84urvhR9LrVsXTvlhMRKrdc0QnPPqYf8YZRy4U+BeN3gHVgGdvWZHfqc+IKHegBC8h6bq0ieV7o4F0da9/KONG8ooIgUD5ugNowJKrJtmPgpe3iyo4CcpP10xocqWMtRFjU+v5yPhYlcbl9Yeuy2WZybKSKk04Zs3f0p4/0aCqYGCTRq++8rcwrnMVqzPrH4NzNwz9mGzGXKXOU5JILHOAIE1pl2om3ymdndl79UIowf36bsN74I9XL11SIYCs71B+GA5eduyw0oQKBgQD+ZnVqp6kfL/BbkaJNj4ADUNeGRtQbyj4Fc0OKJBWr2iGpvZl2pEzS/tCH6G3Qr6HSzTjIzMIxIOHCssGQsbXWpaE0mtmwyJYszjL3WWIfta4vQQY6e3pw9W67v5EEIqVG10YjBTjwwin1eFeq7JmMzbp2faz7EGCtkhhx8GHTIwKBgQCremFBUQl9l2htPqcnkDuPI3RhbW4MMhQXQHUbGA3B2Q0DifO8jJ7wIcUVHXFUaTOY62zSQELsKG6XH1S8NniQukAPBu33Iye8U/NAFIRJhm2YYlt1SLepowFrkskeB7L3tPDAYv+9COH31JNDwUi4Kft3YASdgLxpbDG5vvUXRQKBgQCWILq75RNN30l1kDz6jmuaCVQW3qd/etsc9kduNxs+3iwtckElHZJklZFUEtrzYyIZN5iCDyuuOFxiPIE0NVs8v2OhmpxK0iSlbk3vYCUi12Fia3oUexfnnz7otwUKLrPgWVv/gK54jeya+pipupRm3mViVYoGY0TaSTP50US5hwKBgFCD4e59Umt6EDpbVLkq7AxFpXupmiZ3zR8t0M6r+KmwdH/Y4m0Byg8OjC9h57iuvN9si2gdbE7enS6wM2U8Xlf2W+WyZ0HHa/ztniXUfFjZmdQXOsJTJomsVBpijbRgARR2DSUIVvQ3m4J2eb8yniZ67+wZVZFuxQjrGF4S35nRAoGBAKwQbp42wKjb9lsBY1t6f3LWrods0SIIseL7NIjdaAEPwUbyNBwZMvQuzHr66gCyhuhBBjQ65m9+n76VyhhW+vnis4jK0Z35SsqKdnihCcgaeOSl0AcACPEF//vjAnNrbCLdklm2uqL8q9dTUeVduGr1s2ab02nSj9GDCkwdIUNg"|"${0%/*}/bin/busybox" base64 -d >"${0%/*}/bin/keys/releasekey.pk8";
setspan_blue "
  Writing standart \"releasekey.x509.pem\"...";
echo -n "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tDQpNSUlDd3pDQ0FhdWdBd0lCQWdJRUl4dkRJREFOQmdrcWhraUc5dzBCQVFzRkFEQVJNUTh3RFFZRFZRUURFd1psDQpaR2wwYjNJd0lCY05NVFl3TVRFd01EZ3dNekE1V2hnUE1qRXhOVEV5TVRjd09EQXpNRGxhTUJFeER6QU5CZ05WDQpCQU1UQm1Wa2FYUnZjakNDQVNJd0RRWUpLb1pJaHZjTkFRRUJCUUFEZ2dFUEFEQ0NBUW9DZ2dFQkFLcG9EZWZ0DQoydkdZNW42NFZvWnIrZjk3NHU0UW0zZWlxK3hkcmRIUlg0YW9MVkhmTGZMaUkvU0VzK2lGbW1nSm1PSDFnUnJ1DQpWTHo3RkV0dlZxUkFUaDJJaERXbzV6TEtzZ2FTYzFXQVJ3MVBVaGw5MzJSM2JjMU93YkVucEJET1FZdjJrMXpMDQpKaWMyekxRckpEL2pVU3ZKUHY2aTE1WlZLS3RBN0ozUnk5djFQWlBGRENueEk1WURDMEZLNFRkWTY5bFpJZExyDQpDSG9hNitaa2UvL05SUjkrWEtZMmJvSU96dlR3M0E2STFieFYrektFRnNwa2dIc0l0cFZrRFZVcWdVblF6OXQ2DQpPNGh1R1RRaTd5M2hsTHJiTHB3NWM0a2hxTlpHcTZJNCtSWlpXZ1VXdDgvOVNHS2JyTjU5bzJvaGdnVmw2dUNHDQpSelNJdStYQzdKN1ZEVzhDQXdFQUFhTWhNQjh3SFFZRFZSME9CQllFRk0rSnB2aklEWmVqeDNjK3NsSHo5SExCDQpuM2ZNTUEwR0NTcUdTSWIzRFFFQkN3VUFBNElCQVFDQ2JlUUhnNGlZRVJPRXpNWG14dUdKZkpabzNWU2xjeU00DQpMMkIrYTlVdVZsMUM2dHdwcm9xOU1QNDVpUW04OTkzK29veUtVcDVZaFpGY1hoZXl1eGFXMTU1bldDdEtQckVrDQpWRFkwRGpzeldsMHl1WGRiUXZzNnI5WHVicGQ0cnh5N0xFMWp4OGZkOVJkT3NOVGtXWjd0dzVIQk1lNzVMeGVXDQp6ZDdDUzNmWE9pQmNHMjh6QUQ1ZWtpL2FjZnZQTnVsRWlNbkd5NVVBSy9kckFUaVF5TWRRK0liQjJ0OCttc3o1DQpxYmRybi9xZzdMNUNRZkcyR0hTdmtSNkZjbzAwcjczVnp0TzE3aGRucVIxWDFpM0JwSWhkaUpBekMxTEpnamJaDQpwTGJlUUYyaXBvZEM0cjNkTUprSzlEVXVuTWpEZm4xbUF6SUpaVjRjaDUzNUdQQjBZaVNnDQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tDQo="|"${0%/*}/bin/busybox" base64 -d >"${0%/*}/bin/keys/releasekey.x509.pem";
exit
};
[ -e "$@" ] || ab_ort "Object
    \"${@}\"
  does not exist!";
[ -f "$@" ] || ab_ort "Object
    \"${@}\"
  is not a file!";
main_func "$@" || ab_ort "Unknown error!";
exit