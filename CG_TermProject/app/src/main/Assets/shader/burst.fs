precision mediump float;

 uniform sampler2D s_tex0;
 uniform float materialSh;
 uniform vec3 sourceDiff, sourceSpec, sourceAmbi;  // Sd, Ss, Sa

 varying vec3 v_normal;
 varying vec2 v_texCoord;
 varying vec3 v_lightDir, v_viewDir;


 void main() {
 	gl_FragColor =texture2D(s_tex0, v_texCoord);
 }