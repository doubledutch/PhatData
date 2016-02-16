function renderExplorerInput(data){
	var buf=''
	buf+='<form name="sample" id="sample">'
	buf+='<input type="text" name="count" value="100">'
	buf+='<select name="type">'
	buf+='<option value="first">First</option>'
	buf+='<option value="last">Last</option>'
	buf+='<option value="random" selected="selected">Random</option>'
	buf+='</select>'
	buf+='<input type="button" value="Run Sample" onclick="startExplorerSample()">'
	buf+='</form>'
	return buf
}

var stateNames=['Submitted','Compiling','Running','Done','Failed']

function renderExplorerOutput(data){
	var buf=''
	var sample=data.get('sample')
	if(sample.has('id')){
		buf+='<div class="sample-container">'
		buf+='<div class="sample-state">'
		var state=sample.get('state')
		buf+='State: '+stateNames[state]
		buf+=' Time: '+sample.get('time')
		buf+='</div>'
		buf+='<div>'
		var results=sample.get('results')
		for(var i=0;i<results.size;i++){
			var result=results.get(i)
			buf+='<div style="border-bottom:1px solid #AAAAAA;padding:10px;">'
			buf+='<div style="word-wrap:break-word;display:inline-block;width:50%;border-right:1px solid #AAAAAA;margin-right:-1px;">'
			buf+=result.get('in')
			buf+='</div><div style="word-wrap:break-word;display:inline-block;width:50%">'
			buf+=result.get('out')
			buf+='</div>'
			buf+='</div>'
		}
		buf+='</div>'
		buf+='</div>'
	}
	return buf
}

function renderExplorerPage(data){
	var buf=''
	buf+=renderExplorerInput(data)
	buf+=renderExplorerOutput(data)
	$('#content').html(buf)
}