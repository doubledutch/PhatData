function renderMenu(data){
	var pages=data.get('pages')
	var buf=''
	for(var i=0;i<pages.size;i++){
		buf+='<div class="menu-item'
		if(data.get('current_page')==i){
			buf+=' menu-select'
		}
		buf+='" onclick="showMenu('+i+')">'
		buf+=pages.get(i).get('title')
		buf+='</div>'
	}
	$('#menu').html(buf)
}

function renderPage(data){
	switch(data.get('current_page')){
		case 0:renderExplorerPage(data)
			break
//		case 0:renderStreamsPage(data)
//			break
//		case 1:renderTasksPage(data)
//			break
	}
}

function render(){
	renderMenu(state)
	renderPage(state)
}