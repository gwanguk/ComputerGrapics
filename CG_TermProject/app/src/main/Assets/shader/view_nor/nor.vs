// transform uniform value
uniform mat4 worldMat; // to world space
uniform mat4 viewMat; // to camera space
uniform mat4 projMat; // to clip space


uniform vec3 eyePos; // worldspace 에서의 eyePosition
uniform vec3 lightPos; // worldsapce 에서의 lightPostion

attribute vec3 position; // object position vector
attribute vec3 normal; //object 의 normal vector
attribute vec2 texCoord; // texture coordinates
attribute vec3 tangent; // tangent space의 tangent vector


//fragment shader로 넘겨줄 values
varying vec2 v_texCoord; // texture coordinates
varying vec3 v_lightWS, v_viewWS; // world space의 light position, view position
varying mat3 TangentToWorldSpace; // Tangentspace 에서 worldspace로의 변환 matrix

void main() {
   // you should fill in this function
       vec4 posWS = worldMat * vec4(position, 1.0); // polygon mesh의 world space position
       gl_Position = projMat * viewMat * posWS; // view transform 과 projection transform 이후의 clipspace에서의 position
       v_texCoord = texCoord; // texture coordinates 은 그대로 넘겨줌

       // per - vertex TBN-basis 구하기
       vec3 nor = mat3(worldMat) * normal; //N : world space 에 대한 tangent plane의 surface normal vector
       vec3 tan = mat3(worldMat) * normalize(tangent); // T :주어진 tan vector을 world space 에 대한 tangent space 의 tangent vector로 변환
       vec3 bin = cross(nor,tan); //B : normal과 tangent를 cross product 하여 binormal vector 구함

       //TBN-basis 를 통한 Tangent to World space 로의 회전 매트릭스 구하기
       TangentToWorldSpace = mat3(tan.x, tan.y, tan.z,
                                  bin.x, bin.y, bin.z,
                                  nor.x, nor.y, nor.z);
                                  //| T.x, B.x N.x, |
                                  //| T.y, B.y N.y, |
                                  //| T.z, B.z N.z  |


       v_lightWS = normalize(lightPos - posWS.xyz); // l vector : world space에서의 vertex에서 light 방향의 unit vector
       v_viewWS = normalize(eyePos - posWS.xyz); // v vector : world space에서의 vertex에서 view 방향의 unit vector
}