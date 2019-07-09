import { Pipe, PipeTransform} from '@angular/core';

@Pipe({ name: 'highlight'})

export class HighlightSearch {
  transform(text:string, search: string): string {
    let pattern =  search.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");
   // console.log(pattern);
   // pattern = pattern.split(' ').filter((t) => { return t.length > 0; }).join('|');
    let regex = new RegExp(pattern, 'gi');
    return search ? text.replace(regex, (match) => `<mark>${match}</mark>`) : text;
  }
}
