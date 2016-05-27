precision mediump float;

uniform sampler2D s_tex0; // image texture uniform
uniform sampler2D s_texNor; // normal map

uniform vec3 materialDiff, materialSpec, 		  // Md, Ms, Ma, Me
			 materialAmbi, materialEmit;
uniform float materialSh; // shininess (specular power)
uniform vec3 sourceDiff, sourceSpec, sourceAmbi;  // Sd, Ss, Sa

//vertex shader에서 넘겨 받은 values
varying vec2 v_texCoord; // texture coordiantes
varying vec3 v_lightWS, v_viewWS; //  world space에서의 l, v vector
varying mat3 TangentToWorldSpace; // tangent space 에서 world space 로의 회전 변환

struct Material {
	float sh; 					 // shininess (specular power)
	vec3 diff, spec, ambi, emit; // material colors
};

struct Light {
	vec3 dir, diff, spec, ambi;  // light direction and colors
};

vec3 phongLight(vec3 view, vec3 normal, // view direction and normal
				Material M, Light S) {  // material and source light
	float diff = max(dot(normal, S.dir), 0.0); // diffuse term 에서의 intencity
	vec3 refl = 2.0 * normal * dot(normal, S.dir) - S.dir; // reflection vector
	float spec = 0.0;
	if(diff > 0.0) spec = pow(max(dot(refl, view), 0.0), M.sh); // specular term 에서의 intencity

	vec3 sum = vec3(0.0);
	sum += diff * S.diff * M.diff;  	// add diffuse term
	sum += spec * S.spec * M.spec;  	// add specular term
	sum += S.ambi * M.ambi + M.emit; 	// add ambient and emissive term

	return sum;
}


void main() {
   	vec3 materialDiff = texture2D(s_tex0, v_texCoord).xyz; // texture coordinates를 이용하여 image texture 에서 해당 RGB 값을 추출하여 materialdiff로 사용
	vec3 norWS= TangentToWorldSpace * normalize(texture2D(s_texNor, v_texCoord).xyz * 2.0 - vec3(1.0));
                   	// normal map 에서 texture coordinate에 해당하는 값을 추출하여 normalize(bilinear interporation) 후 [0,1] -> [-1,1] 의 값 범위 변경을 위한
                   	// *2.0 -1.0 연산
	// normal map 에서 추출한 normal value는 tangent space 이기 때문에 vertex shader에서 넘겨 받은 변환 매트릭스를 이용해 worldspace로 변환
   	Material material = Material(materialSh, materialDiff, materialSpec, materialAmbi, materialEmit); // Material 구조체 초기화
   	Light source =  Light(normalize(v_lightWS), sourceDiff, sourceSpec, sourceAmbi); //source 구조체 초기화
   	//v_lightWS vector interporation에 의한 normalize 실시, phonglight에 사용할 worldspace에서의 l vector

    vec3 color =  phongLight(normalize(v_viewWS), normalize(norWS), material, source); //phongLight 함수를 통한 최종 color 값 계산
    // norWS은 worldspace로 변환된 normal vector
    // v_viewWS vector interporation에 의한 normalize 실시,  phonglight에 사용할 worldspace에서의 v vector

   	gl_FragColor = vec4(color, 1.0); //최종 Fragment color
}
